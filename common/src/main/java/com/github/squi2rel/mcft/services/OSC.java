package com.github.squi2rel.mcft.services;

import com.github.squi2rel.mcft.AutoBlink;
import com.github.squi2rel.mcft.MCFT;
import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.transport.OSCPortIn;
import com.illposed.osc.transport.OSCPortOut;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.github.squi2rel.mcft.FTModel.model;
import static com.github.squi2rel.mcft.MCFTClient.config;

public class OSC {
    private static final Object lock = new Object();
    private static final Map<String, Float> pendingParameters = new ConcurrentHashMap<>();
    private static OSCPortIn receiver;
    public static volatile long lastOscReceived = 0;
    public static volatile long lastReceived = 0;
    public static final Map<String, Consumer<Float>> allParameters = Map.ofEntries(
            Map.entry("EyeLeftX", f -> model.eyeL.rawPos.x = config.eyeOffsetXL + f * -config.eyeXMul),
            Map.entry("EyeLeftY", f -> model.eyeL.rawPos.y = config.eyeOffsetYL + f * -config.eyeYMul),
            Map.entry("EyeLidLeft", f -> {
                if (!config.autoBlink || config.autoSwitchBlink) model.eyeL.percent = f;
            }),
            Map.entry("EyeRightX", f -> model.eyeR.rawPos.x = config.eyeOffsetXR + f * -config.eyeXMul),
            Map.entry("EyeRightY", f -> model.eyeR.rawPos.y = config.eyeOffsetYR + f * -config.eyeYMul),
            Map.entry("EyeLidRight", f -> {
                if (!config.autoBlink || config.autoSwitchBlink) model.eyeR.percent = f;
            }),
            Map.entry("JawOpen", f -> model.mouth.percent = f)
    );

    public static void init() throws Exception {
        InetAddress bindAddress = resolveLoopback(config.oscBindHost, "oscBindHost");
        int receivePort = requirePort(config.oscReceivePort, "oscReceivePort");
        synchronized (lock) {
            if (receiver != null && receiver.isListening()) return;
            closeReceiver();
            OSCPortIn candidate = null;
            try {
                candidate = new OSCPortIn(new InetSocketAddress(bindAddress, receivePort));
                candidate.getDispatcher().addListener(new MessageSelector() {
                    @Override
                    public boolean isInfoRequired() {
                        return false;
                    }

                    @Override
                    public boolean matches(OSCMessageEvent oscMessageEvent) {
                        return true;
                    }
                }, OSC::handleMessage);
                candidate.startListening();
                receiver = candidate;
                candidate = null;
            } finally {
                if (candidate != null) candidate.close();
            }
        }
        MCFT.LOGGER.info("OSC started on {}:{}", bindAddress.getHostAddress(), receivePort);
        try {
            InetAddress targetAddress = resolveLoopback(config.oscTargetHost, "oscTargetHost");
            int sendPort = requirePort(config.oscSendPort, "oscSendPort");
            sendAvatarChange(targetAddress, sendPort);
        } catch (Exception e) {
            MCFT.LOGGER.error("OSC avatar change target is invalid", e);
        }
    }

    public static void applyPendingParameters() {
        lastReceived = AutoBlink.enabled ? System.currentTimeMillis() : lastOscReceived;
        pendingParameters.forEach((name, value) -> {
            if (pendingParameters.remove(name, value)) {
                try {
                    allParameters.get(name).accept(value);
                } catch (RuntimeException e) {
                    MCFT.LOGGER.error("Failed to apply OSC parameter {}", name, e);
                }
            }
        });
    }

    public static void shutdown() {
        synchronized (lock) {
            try {
                closeReceiver();
            } catch (IOException e) {
                MCFT.LOGGER.error("Failed to close OSC receiver", e);
            }
            pendingParameters.clear();
        }
    }

    private static void handleMessage(OSCMessageEvent event) {
        String address = "unknown";
        try {
            OSCMessage message = event.getMessage();
            address = message.getAddress();
            if (!address.startsWith("/v2/")) return;
            String parameter = address.substring(4);
            if (!allParameters.containsKey(parameter)) return;
            List<Object> arguments = message.getArguments();
            if (arguments.size() != 1 || !(arguments.get(0) instanceof Number value)) {
                MCFT.LOGGER.warn("Ignored invalid OSC arguments for {}", address);
                return;
            }
            float argument = value.floatValue();
            if (!Float.isFinite(argument)) {
                MCFT.LOGGER.warn("Ignored non-finite OSC argument for {}", address);
                return;
            }
            lastOscReceived = System.currentTimeMillis();
            pendingParameters.put(parameter, argument);
        } catch (RuntimeException e) {
            MCFT.LOGGER.error("Failed to handle OSC message {}", address, e);
        }
    }

    private static void sendAvatarChange(InetAddress targetAddress, int sendPort) {
        UUID uuid = MinecraftClient.getInstance().getSession().getUuidOrNull();
        if (uuid == null) {
            MCFT.LOGGER.warn("Skipped OSC avatar change without a session UUID");
            return;
        }
        OSCPortOut sender = null;
        try {
            sender = new OSCPortOut(new InetSocketAddress(targetAddress, sendPort));
            sender.send(new OSCMessage("/avatar/change", List.of(uuid.toString())));
        } catch (Exception e) {
            MCFT.LOGGER.error("Failed to send OSC avatar change to {}:{}", targetAddress.getHostAddress(), sendPort, e);
        } finally {
            if (sender != null) {
                try {
                    sender.close();
                } catch (IOException e) {
                    MCFT.LOGGER.error("Failed to close OSC sender", e);
                }
            }
        }
    }

    private static InetAddress resolveLoopback(String host, String setting) throws IOException {
        if (host == null || host.isBlank()) throw new IllegalArgumentException(setting + " must not be blank");
        InetAddress address = InetAddress.getByName(host);
        if (!address.isLoopbackAddress()) throw new IllegalArgumentException(setting + " must resolve to a loopback address");
        return address;
    }

    private static int requirePort(int port, String setting) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException(setting + " must be between 1 and 65535");
        return port;
    }

    private static void closeReceiver() throws IOException {
        if (receiver == null) return;
        OSCPortIn current = receiver;
        receiver = null;
        current.close();
    }
}
