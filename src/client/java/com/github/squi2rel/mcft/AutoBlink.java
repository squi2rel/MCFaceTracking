package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.services.OSC;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.Random;

import static com.github.squi2rel.mcft.FTModel.model;
import static com.github.squi2rel.mcft.MCFTClient.config;

public class AutoBlink {
    public static boolean enabled;
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
            if (!config.autoBlink || config.autoSwitchBlink && OSC.lastReceived != lastUpdateTime && System.currentTimeMillis() - OSC.lastReceived < 3000) {
                enabled = false;
                return;
            }
            enabled = true;
            update();
            model.eyeL.percent = model.eyeR.percent = eyeOpenness * config.blinkMaxY;
            OSC.lastReceived = lastUpdateTime;
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
            blinkDuration = config.blinkDuration * 1000 + random.nextFloat() * config.blinkDurationFix * 1000;
            blinkInterval = config.blinkInterval * 1000 + random.nextFloat() * config.blinkIntervalFix * 1000;
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
