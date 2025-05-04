package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class Rect {
    public float ix, iy, iw, ih;
    public float x, y, w, h;

    public Rect() {
        this(0, 0, 0, 0);
    }

    public Rect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        ix = x;
        iy = y;
        iw = w;
        ih = h;
    }

    public Rect(float x, float y, float w, float h, float ix, float iy, float iw, float ih) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.ix = ix;
        this.iy = iy;
        this.iw = iw;
        this.ih = ih;
    }

    public static void writePos(Rect rect, ByteBuf buf) {
        buf.writeFloat(rect.x);
        buf.writeFloat(rect.y);
    }

    public static void readPos(Rect rect, ByteBuf buf) {
        rect.x = buf.readFloat();
        rect.y = buf.readFloat();
    }

    public static void write(Rect rect, ByteBuf buf) {
        buf.writeBoolean(rect != null);
        if (rect != null) {
            buf.writeFloat(rect.x);
            buf.writeFloat(rect.y);
            buf.writeFloat(rect.w);
            buf.writeFloat(rect.h);
            buf.writeFloat(rect.ix);
            buf.writeFloat(rect.iy);
            buf.writeFloat(rect.iw);
            buf.writeFloat(rect.ih);
        }
    }

    public static Rect read(ByteBuf buf) {
        if (buf.readBoolean()) {
            return new Rect(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
        return null;
    }
}
