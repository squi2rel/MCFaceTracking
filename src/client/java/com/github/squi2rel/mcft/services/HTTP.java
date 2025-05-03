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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class HTTP {
    public static final int port = 8999;
    public static void init() throws IOException {
        createInfo();
        Thread http = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    try (Socket s = serverSocket.accept()) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        while (true) {
                            String headerLine = in.readLine();
                            if (headerLine == null || headerLine.isEmpty()) {
                                break;
                            }
                        }
                        String response = getString();
                        OutputStream out = s.getOutputStream();
                        out.write(response.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch (IOException ignored) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        http.setDaemon(true);
        http.start();
        MCFT.LOGGER.info("HTTP started on port {}", port);
    }

    private static String getString() throws IOException {
        String jsonBody = generateJsonData(MinecraftClient.getInstance().getSession().getUuidOrNull());
        byte[] jsonBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        return "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: " + jsonBytes.length + "\r\nConnection: close\r\n\r\n" + jsonBody;
    }

    private static void createInfo() throws IOException { //v2
        File root = new File(System.getenv("localappdata") + "Low", "VRChat/VRChat/OSC/MCFT/Avatars");
        root.mkdirs();
        Session s = MinecraftClient.getInstance().getSession();
        File child = new File(root, s.getUuidOrNull() + ".json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tree = mapper.createObjectNode();
        tree.put("id", s.getUuidOrNull().toString());
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