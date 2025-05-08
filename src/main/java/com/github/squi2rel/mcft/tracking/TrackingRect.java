package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class TrackingRect extends Rect {
    public float u1, v1, u2, v2;

    public TrackingRect() {
        super(0, 0, 0, 0);
    }

    public TrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public TrackingRect(float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
        super(x, y, w, h);
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float w, float h, float u1, float v1, float u2, float v2) {
        this.w = w;
        this.h = h;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    public void uv(float u1, float v1, float u2, float v2) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    public void writeSync(ByteBuf buf) {
    }

    public void readSync(ByteBuf buf) {
    }

    public void write(ByteBuf buf) {
        Rect.write(new Rect(x, y, w, h, u1, v1, u2, v2), buf);
    }

    public void update() {
    }

    public void validate(boolean init) {
        if (!init) return;
        checkInRange(u1, 0, 1);
        checkInRange(v1, 0, 1);
        checkInRange(u2, 0, 1);
        checkInRange(v2, 0, 1);
    }

    public static void checkInRange(float val, float min, float max) {
        if (!(val >= min && val <= max)) throw new IllegalArgumentException(String.format("%f should be between %f and %f", val, min, max));
    }
}
