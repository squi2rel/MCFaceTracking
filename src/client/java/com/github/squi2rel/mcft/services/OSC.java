package com.github.squi2rel.mcft.services;

import com.github.squi2rel.mcft.MCFT;
import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.transport.OSCPortIn;
import com.illposed.osc.transport.OSCPortOut;
import net.minecraft.client.MinecraftClient;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.squi2rel.mcft.FTModel.model;
import static com.github.squi2rel.mcft.MCFTClient.config;

public class OSC {
    public static long lastReceived = 0;
    public static Map<String, Consumer<List<Object>>> allParameters = Map.ofEntries(
            Map.entry("EyeLeftX", f -> model.eyeL.rawPos.x = (float) f.getFirst() * -config.eyeXMul),
            Map.entry("EyeLeftY", f -> model.eyeL.rawPos.y = (float) f.getFirst() * -config.eyeYMul),
            Map.entry("EyeLidLeft", f -> {
                if (!config.autoBlink) model.eyeL.percent = (float) f.getFirst();
            }),
            Map.entry("EyeRightX", f -> model.eyeR.rawPos.x = (float) f.getFirst() * -config.eyeXMul),
            Map.entry("EyeRightY", f -> model.eyeR.rawPos.y = (float) f.getFirst() * -config.eyeYMul),
            Map.entry("EyeLidRight", f -> {
                if (!config.autoBlink) model.eyeR.percent = (float) f.getFirst();
            }),
            Map.entry("JawOpen", f -> model.mouth.percent = (float) f.getFirst())
    );

    public static void init() throws Exception {
        OSCPortIn receiver = new OSCPortIn(new InetSocketAddress("localhost", config.oscReceivePort));
        OSCPortOut sender = new OSCPortOut(new InetSocketAddress("localhost", config.oscSendPort));
        receiver.getDispatcher().addListener(new MessageSelector() {
            @Override
            public boolean isInfoRequired() {
                return false;
            }

            @Override
            public boolean matches(OSCMessageEvent oscMessageEvent) {
                return true;
            }
        }, e -> {
            OSCMessage msg = e.getMessage();
            Consumer<List<Object>> c = allParameters.get(msg.getAddress().replace("/v2/", ""));
            if (c != null) {
                lastReceived = System.currentTimeMillis();
                c.accept(msg.getArguments());
            }
        });
        receiver.startListening();
        MCFT.LOGGER.info("OSC started on port {}", 9000);
        sender.send(new OSCMessage("/avatar/change", List.of(MinecraftClient.getInstance().getSession().getUuidOrNull().toString())));
    }
}
