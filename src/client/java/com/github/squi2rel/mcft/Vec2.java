package com.github.squi2rel.mcft;

import net.minecraft.util.math.Vec2f;

public class Vec2 {
    public static Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2(), v4 = new Vec2();
    float x, y;

    public Vec2() {
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 x(float x) {
        this.x = x;
        return this;
    }

    public Vec2 y(float y) {
        this.y = y;
        return this;
    }

    public Vec2 set(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vec2 set(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vec2f vec2f() {
        return new Vec2f(x, y);
    }

    public Vec2 copy() {
        return new Vec2(this);
    }

    public Vec2 add(Vec2 v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vec2 sub(Vec2 v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vec2 mul(Vec2 v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vec2 div(Vec2 v) {
        this.x /= v.x;
        this.y /= v.y;
        return this;
    }

    public Vec2 neg() {
        return new Vec2(-x, -y);
    }

    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vec2 normalize() {
        float len = len();
        x /= len;
        y /= len;
        return this;
    }

    public Vec2 lerp(Vec2 v, float alpha) {
        float invAlpha = 1.0f - alpha;
        x = (x * invAlpha) + (v.x * alpha);
        y = (y * invAlpha) + (v.y * alpha);
        return this;
    }
}
