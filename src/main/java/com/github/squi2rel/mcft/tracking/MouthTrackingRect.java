package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.MathHelper;

public class MouthTrackingRect extends TrackingRect {
    public transient float percent;
    public transient float lastPercent;

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
        lastPercent = percent;
        percent = buf.readFloat();
    }

    @Override
    public void update(float delta) {
        float p = MathHelper.lerp(delta, lastPercent, percent);
        float r = Math.max(p - 0.05f, 0f) / 0.95f;
        h = ih * r;
        y = iy - ih * (1 - r);
        float a = ih * p;
        x = ix + a;
        w = iw - a * 2;
    }

    @Override
    public void validate(boolean init) {
        if (init) {
            super.validate(true);
            checkInRange(x, 0, 8);
            checkInRange(y, 0, 8);
            checkInRange(w, 0, 8 - x);
            checkInRange(h, -3, 8);
        } else {
            checkInRange(percent, 0, 1);
        }
    }

    public static MouthTrackingRect read(ByteBuf buf) {
        Rect rect = Rect.read(buf);
        if (rect == null) return null;
        return new MouthTrackingRect(rect.x, rect.y, rect.w, rect.h, rect.ix, rect.iy, rect.iw, rect.ih);
    }
}
