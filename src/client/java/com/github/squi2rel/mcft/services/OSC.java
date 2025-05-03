package com.github.squi2rel.mcft.services;

import com.github.squi2rel.mcft.FTModel;
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

public class OSC {

    public static Map<String, Consumer<List<Object>>> allParameters = Map.ofEntries(
            Map.entry("EyeLeftX", f -> FTModel.eyeL.rawPos.x((float) f.getFirst() * 0.3f)),
            Map.entry("EyeLeftY", f -> FTModel.eyeL.rawPos.y((float) f.getFirst() * 0.5f)),
            Map.entry("EyeLidLeft", f -> FTModel.eyeL.percent = (float) f.getFirst()),
            Map.entry("EyeRightX", f -> FTModel.eyeR.rawPos.x((float) f.getFirst() * 0.3f)),
            Map.entry("EyeRightY", f -> FTModel.eyeR.rawPos.y((float) f.getFirst() * 0.5f)),
            Map.entry("EyeLidRight", f -> FTModel.eyeR.percent = (float) f.getFirst()),
            Map.entry("JawOpen", f -> FTModel.mouth.percent = (float) f.getFirst())
    );

    public static void init() throws Exception {
        OSCPortIn receiver = new OSCPortIn(new InetSocketAddress("localhost", 9000));
        OSCPortOut sender = new OSCPortOut(new InetSocketAddress("localhost", 9001));
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
            if (c != null) c.accept(msg.getArguments());
        });
        receiver.startListening();
        MCFT.LOGGER.info("OSC started on port {}", 9000);
        sender.send(new OSCMessage("/avatar/change", List.of(MinecraftClient.getInstance().getSession().getUuidOrNull().toString())));
    }
}
