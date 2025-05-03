package com.github.squi2rel.mcft.tracking;

public class MouthTrackingRect extends TrackingRect {
    public float percent;
    public float tongueOut;

    public MouthTrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void update() {
         float r = Math.max(percent - 0.15f, 0f) / 0.85f;
         h = ih * r;
         y = iy - ih * (1 - r);
         float a = ih * percent;
         x = ix + a;
         w = iw - a * 2;
    }
}
