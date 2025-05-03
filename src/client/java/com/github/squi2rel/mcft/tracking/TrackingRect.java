package com.github.squi2rel.mcft.tracking;

public abstract class TrackingRect {
    public float x, y, w, h;
    public final float ix, iy, iw, ih;

    public TrackingRect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        ix = x;
        iy = y;
        iw = w;
        ih = h;
    }

    public abstract void update();
}
