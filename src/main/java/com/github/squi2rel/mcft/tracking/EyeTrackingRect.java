package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class EyeTrackingRect extends TrackingRect {
    public transient float percent;
    public Rect rawPos = new Rect();
    public SizeTrackingRect ball = new SizeTrackingRect();
    public TrackingRect lid = new SizeTrackingRect();
    public TrackingRect inner = new SizeTrackingRect();

    public EyeTrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void writeSync(ByteBuf buf) {
        buf.writeFloat(percent);
        Rect.writePos(rawPos, buf);
        ball.writeSync(buf);
    }

    @Override
    public void readSync(ByteBuf buf) {
        percent = buf.readFloat();
        Rect.readPos(rawPos, buf);
        ball.readSync(buf);
    }

    protected EyeTrackingRect(
            float x, float y, float w, float h,
            float u1, float v1, float u2, float v2,
            float ew, float eh, float eu1, float eu2, float ev1, float ev2,
            float lw, float lh, float lu1, float lv1, float lu2, float lv2,
            float iw, float ih, float iu1, float iu2, float iv1, float iv2
    ) {
        super(x, y, w, h, u1, v1, u2, v2);
        ball.set(ew, eh, eu1, ev1, eu2, ev2);
        lid.set(lw, lh, lu1, lv1, lu2, lv2);
        inner.set(iw, ih, iu1, iu2, iv1, iv2);
    }

    @Override
    public void update() {
        ball.set(rawPos.x, rawPos.y);
        h = ih * percent;
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        ball.write(buf);
        lid.write(buf);
        inner.write(buf);
    }

    public static EyeTrackingRect read(ByteBuf buf) {
        Rect rect = Rect.read(buf);
        Rect ball = Rect.read(buf);
        Rect lid = Rect.read(buf);
        Rect inner = Rect.read(buf);
        if (rect == null || ball == null || lid == null || inner == null) throw new IllegalStateException();
        return new EyeTrackingRect(
                rect.x, rect.y, rect.w, rect.h,
                rect.ix, rect.iy, rect.iw, rect.ih,
                ball.x, ball.y, ball.ix, ball.iy, ball.iw, ball.ih,
                lid.x, lid.y, lid.ix, lid.iy, lid.iw, lid.ih,
                inner.x, inner.y, inner.ix, inner.iy, inner.iw, inner.ih
        );
    }
}
