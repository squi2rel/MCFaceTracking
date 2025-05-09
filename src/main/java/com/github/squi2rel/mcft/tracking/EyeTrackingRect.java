package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class EyeTrackingRect extends TrackingRect {
    public transient float percent;
    public Rect rawPos = new Rect();
    public TrackingRect ball = new TrackingRect();
    public TrackingRect lid = new TrackingRect();
    public TrackingRect inner = new TrackingRect();

    public EyeTrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void writeSync(ByteBuf buf) {
        buf.writeFloat(percent);
        Rect.writePos(rawPos, buf);
    }

    @Override
    public void readSync(ByteBuf buf) {
        percent = buf.readFloat();
        Rect.readPos(rawPos, buf);
    }

    protected EyeTrackingRect(
            float x, float y, float w, float h,
            float u1, float v1, float u2, float v2,
            float ew, float eh, float eu1, float eu2, float ev1, float ev2,
            float lu1, float lv1, float lu2, float lv2,
            float iu1, float iv1, float iu2, float iv2
    ) {
        super(x, y, w, h, u1, v1, u2, v2);
        ball.set(ew, eh, eu1, ev1, eu2, ev2);
        lid.uv(lu1, lv1, lu2, lv2);
        inner.uv(iu1, iv1, iu2, iv2);
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
        Rect.write(tmp.set(lid.u1, lid.v1, lid.u2, lid.v2, inner.u1, inner.v1, inner.u2, inner.v2), buf);
    }

    @Override
    public void validate(boolean init) {
        if (init) {
            super.validate(true);
            ball.validate(true);
            lid.validate(true);
            inner.validate(true);
            checkInRange(ball.w, 0, 8 - ball.x);
            checkInRange(ball.h, 0, 8 - ball.y);
            checkInRange(lid.w, 0, 8 - lid.x);
            checkInRange(lid.h, 0, 8 - lid.y);
            checkInRange(inner.w, 0, 8 - inner.x);
            checkInRange(inner.h, 0, 8 - inner.y);
        } else {
            checkInRange(percent, 0, 1);
            checkInRange(rawPos.x, -2, 2);
            checkInRange(rawPos.y, -2, 2);
        }
    }

    public static EyeTrackingRect read(ByteBuf buf) {
        Rect rect = Rect.read(buf);
        Rect ball = Rect.read(buf);
        Rect inner = Rect.read(buf);
        if (rect == null || ball == null || inner == null) throw new IllegalArgumentException();
        return new EyeTrackingRect(
                rect.x, rect.y, rect.w, rect.h,
                rect.ix, rect.iy, rect.iw, rect.ih,
                ball.w, ball.h, ball.ix, ball.iy, ball.iw, ball.ih,
                inner.x, inner.y, inner.w, inner.h,
                inner.ix, inner.iy, inner.iw, inner.ih
        );
    }
}
