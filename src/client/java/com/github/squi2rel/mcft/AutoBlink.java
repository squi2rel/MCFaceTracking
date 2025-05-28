package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.services.OSC;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.Random;

import static com.github.squi2rel.mcft.FTModel.model;

public class AutoBlink {
    private static float blinkTime = 0f;
    private static float blinkDuration = 300f;
    private static float blinkInterval = 3000f;
    private static boolean blinking = false;
    private static float eyeOpenness = 1f;
    private static float last = 0f;
    private static final Random random = new Random();

    private static long lastUpdateTime = System.currentTimeMillis();

    public static void init() {
        WorldRenderEvents.LAST.register(e -> {
            if (!MCFTClient.config.autoBlink) return;
            update();
            model.eyeL.percent = model.eyeR.percent = eyeOpenness;
            OSC.lastReceived = System.currentTimeMillis();
        });
    }

    public static void update() {
        long currentTime = System.currentTimeMillis();
        float delta = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        last += delta;

        if (!blinking && last >= blinkInterval) {
            blinking = true;
            blinkTime = 0f;
            last = 0f;
            blinkDuration = 100 + random.nextFloat() * 100;
            blinkInterval = 2500 + random.nextFloat() * 1000;
        }

        if (blinking) {
            blinkTime += delta;
            float t = blinkTime / blinkDuration;

            if (t >= 1f) {
                blinking = false;
                eyeOpenness = 1f;
            } else {
                eyeOpenness = (float) (1 - Math.sin(t * Math.PI));
            }
        }
    }
}
