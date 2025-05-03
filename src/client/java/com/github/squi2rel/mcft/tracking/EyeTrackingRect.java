package com.github.squi2rel.mcft.tracking;

import com.github.squi2rel.mcft.Vec2;

public class EyeTrackingRect extends TrackingRect {
    public float percent;
    public Vec2 rawPos = new Vec2(), pos = new Vec2();
    public Vec2 ball = new Vec2(0.75f, 0.75f);

    public EyeTrackingRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void update() {
        pos.set(rawPos);
        h = ih * percent;
    }
}
