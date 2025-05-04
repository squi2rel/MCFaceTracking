package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class MouthTrackingRect extends TrackingRect {
    public transient float percent;

    public MouthTrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public MouthTrackingRect(float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
        super(x, y, w, h, u1, v1, u2, v2);
    }

    @Override
    public void writeSync(ByteBuf buf) {
        buf.writeFloat(percent);
    }

    @Override
    public void readSync(ByteBuf buf) {
        percent = buf.readFloat();
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

    public static MouthTrackingRect read(ByteBuf buf) {
        Rect rect = Rect.read(buf);
        if (rect == null) return null;
        return new MouthTrackingRect(rect.x, rect.y, rect.w, rect.h, rect.ix, rect.iy, rect.iw, rect.ih);
    }
}
