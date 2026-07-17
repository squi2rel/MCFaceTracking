package com.github.squi2rel.mcft.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class HTTP {
    private static final Object lifecycleLock = new Object();
    private static final Object lock = new Object();
    private static final String HTTP_LOOPBACK_HOST = "127.0.0.1";
    private static final int SOCKET_READ_TIMEOUT = 5000;
    private static final int MAX_HEADER_BYTES = 16384;
    public static final int port = MCFTClient.config.httpPort;
    private static ServerSocket serverSocket;
    private static Thread httpThread;

    public static void init() {
        synchronized (lifecycleLock) {
            initServices();
        }
    }

    private static void initServices() {
        try {
            OSC.init();
        } catch (Exception e) {
            MCFT.LOGGER.error("OSC start failed", e);
        }
        if (MinecraftClient.getInstance().getSession().getUuidOrNull() == null) {
            MCFT.LOGGER.warn("OSCQuery start skipped without a session UUID");
            return;
        }
        try {
            createInfo();
        } catch (Exception e) {
            MCFT.LOGGER.error("OSC avatar info creation failed", e);
        }
        boolean httpStarted = false;
        try {
            httpStarted = startServer();
        } catch (Exception e) {
            MCFT.LOGGER.error("HTTP start failed", e);
        }
        if (httpStarted) {
            try {
                DNS.init();
            } catch (Exception e) {
                MCFT.LOGGER.error("DNS start failed", e);
            }
        }
    }

    public static void shutdown() {
        synchronized (lifecycleLock) {
            shutdownServices();
        }
    }

    private static void shutdownServices() {
        ServerSocket current;
        synchronized (lock) {
            current = serverSocket;
            serverSocket = null;
            httpThread = null;
        }
        if (current != null) {
            try {
                current.close();
            } catch (IOException e) {
                MCFT.LOGGER.error("Failed to close HTTP server", e);
            }
        }
        OSC.shutdown();
        DNS.shutdown();
    }

    private static boolean startServer() throws IOException {
        synchronized (lock) {
            if (serverSocket != null && !serverSocket.isClosed() && httpThread != null && httpThread.isAlive()) return true;
            requirePort(port);
            ServerSocket candidate = new ServerSocket();
            try {
                candidate.bind(new InetSocketAddress(InetAddress.getByName(HTTP_LOOPBACK_HOST), port));
                Thread thread = new Thread(() -> runServer(candidate));
                thread.setName("MCFT HTTP");
                thread.setDaemon(true);
                serverSocket = candidate;
                httpThread = thread;
                thread.start();
                MCFT.LOGGER.info("HTTP started on {}:{}", HTTP_LOOPBACK_HOST, port);
                return true;
            } catch (IOException | RuntimeException e) {
                if (serverSocket == candidate) {
                    serverSocket = null;
                    httpThread = null;
                }
                try {
                    candidate.close();
                } catch (IOException closeException) {
                    e.addSuppressed(closeException);
                }
                throw e;
            }
        }
    }

    private static void runServer(ServerSocket socket) {
        try {
            while (!socket.isClosed()) {
                Socket client;
                try {
                    client = socket.accept();
                } catch (IOException e) {
                    if (!socket.isClosed()) MCFT.LOGGER.error("HTTP accept failed", e);
                    break;
                }
                try (client) {
                    handleClient(client);
                } catch (SocketTimeoutException | SocketException ignored) {
                } catch (IOException e) {
                    MCFT.LOGGER.error("HTTP request failed", e);
                } catch (RuntimeException e) {
                    MCFT.LOGGER.error("HTTP request failed", e);
                }
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                MCFT.LOGGER.error("Failed to close HTTP server", e);
            }
            boolean stoppedCurrent = false;
            synchronized (lock) {
                if (serverSocket == socket) {
                    serverSocket = null;
                    httpThread = null;
                    stoppedCurrent = true;
                }
            }
            if (stoppedCurrent) DNS.shutdown();
        }
    }

    private static void handleClient(Socket client) throws IOException {
        client.setSoTimeout(SOCKET_READ_TIMEOUT);
        readHeaders(client.getInputStream());
        String response = getString();
        OutputStream out = client.getOutputStream();
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static void readHeaders(InputStream input) throws IOException {
        int third = -1;
        int second = -1;
        int previous = -1;
        for (int count = 0; count < MAX_HEADER_BYTES; count++) {
            int current = input.read();
            if (current == -1) return;
            if (previous == '\n' && current == '\n' || third == '\r' && second == '\n' && previous == '\r' && current == '\n') return;
            third = second;
            second = previous;
            previous = current;
        }
        throw new IOException("HTTP request headers exceed " + MAX_HEADER_BYTES + " bytes");
    }

    private static void requirePort(int value) {
        if (value < 1 || value > 65535) throw new IllegalArgumentException("httpPort must be between 1 and 65535");
    }

    private static String getString() throws IOException {
        String jsonBody = generateJsonData(MinecraftClient.getInstance().getSession().getUuidOrNull());
        byte[] jsonBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        return "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: " + jsonBytes.length + "\r\nConnection: close\r\n\r\n" + jsonBody;
    }

    private static void createInfo() throws IOException { //v2
        File root = new File(System.getenv("localappdata") + "Low", "VRChat/VRChat/OSC/MCFT/Avatars");
        if (!root.exists() && !root.mkdirs()) throw new IOException();
        Session s = MinecraftClient.getInstance().getSession();
        File child = new File(root, s.getUuidOrNull() + ".json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tree = mapper.createObjectNode();
        tree.put("id", Objects.requireNonNull(s.getUuidOrNull()).toString());
        tree.put("name", s.getUsername());
        ArrayNode parameters = mapper.createArrayNode();
        for (String p : OSC.allParameters.keySet()) {
            ObjectNode obj = mapper.createObjectNode();
            obj.put("name", p);
            ObjectNode input = mapper.createObjectNode();
            input.put("address", "/v2/" + p);
            input.put("type", "Float");
            obj.set("input", input);
            parameters.add(obj);
        }
        tree.set("parameters", parameters);
        Files.writeString(child.toPath(), mapper.writeValueAsString(tree));
    }

    private static String generateJsonData(UUID uuid) throws IOException { //v1
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Node root = new Node(n -> {
            n.FULL_PATH = "/avatar";
            n.ACCESS = 0;
            n.CONTENTS = Map.of(
                    "change", new Node(c -> {
                        c.FULL_PATH = n.FULL_PATH + "/change";
                        c.ACCESS = 3;
                        c.TYPE = "s";
                        c.VALUE = uuid.toString();
                    }),
                    "parameters", new Node(c -> {
                        c.FULL_PATH = n.FULL_PATH + "/parameters";
                        c.ACCESS = 0;
                        c.CONTENTS = new HashMap<>();
                        for (String param : OSC.allParameters.keySet()) {
                            c.CONTENTS.put(param, new Node(p -> {
                                p.FULL_PATH = "/v2/" + param;
                                p.ACCESS = 3;
                                p.TYPE = "f";
                                p.VALUE = 0f;
                            }));
                        }
                    }));
        });
        return mapper.writeValueAsString(root);
    }

    public static class Node {
        public String FULL_PATH;
        public int ACCESS;
        public Map<String, Node> CONTENTS;
        public String TYPE;
        @JsonDeserialize(using = ValueDeserializer.class)
        @JsonSerialize(using = ValueSerializer.class)
        public Object VALUE;

        public Node(Consumer<Node> constructor) {
            constructor.accept(this);
        }
    }

    public static class ValueDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isArray() && node.size() == 1) {
                JsonNode element = node.get(0);
                if (element.isBoolean()) return element.asBoolean();
                else if (element.isInt()) return element.asInt();
                else if (element.isDouble()) return element.asDouble();
                else if (element.isTextual()) return element.asText();
            }
            return null;
        }
    }

    public static class ValueSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) return;
            gen.writeStartArray();
            switch (value) {
                case Boolean b -> gen.writeBoolean(b);
                case Float v -> gen.writeNumber(v);
                case String s -> gen.writeString(s);
                default -> throw new IllegalStateException("Unexpected value: " + value);
            }
            gen.writeEndArray();
        }
    }
}
