package com.github.squi2rel.mcft;

public class Config {
    public int httpPort = 8999;
    public int oscReceivePort = 9000;
    public int oscSendPort = 9001;

    public FTModel model = new FTModel();
    public float eyeXMul = 0.5f, eyeYMul = 0.3f;

    public boolean autoBlink = false;
    public float blinkInterval = 5f, blinkIntervalFix = 7.5f, blinkDuration = 0.1f, blinkDurationFix = 0.25f;
    public float blinkMaxY = 0.8f;
}
