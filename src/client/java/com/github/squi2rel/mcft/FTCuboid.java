package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.mixin.client.CuboidAccessor;
import com.github.squi2rel.mcft.mixin.client.VertexAccessor;
import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unchecked", "SameParameterValue"})
public class FTCuboid extends ModelPart.Cuboid {
    public static final Vector3f face = Direction.NORTH.getUnitVector();
    public static UUID player;
    private static final Vector3f position = new Vector3f(), normal = new Vector3f(), tmp = new Vector3f();
    private static final Constructor<FTCuboid> constructor;
    private static int light, overlay, color;

    static {
        try {
            ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
            Constructor<Object> objCtor = Object.class.getDeclaredConstructor();
            constructor = (Constructor<FTCuboid>) rf.newConstructorForSerialization(FTCuboid.class, objCtor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FTCuboid() {
        super(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, 0, 0, Set.of());
        throw new AssertionError();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void renderCuboid(MatrixStack.Entry entry, VertexConsumer buffer, int l, int o, int c) {
        light = l;
        overlay = o;
        color = c;
        Matrix4f posMat = entry.getPositionMatrix();
        for (ModelPart.Quad quad : this.sides) {
            entry.transformNormal(quad.direction(), normal);
            if (quad.direction().equals(face)) {
                FTModel m = MCFTClient.uuidToModel.get(player);
                if (m != null && m.active()) {
                    drawFace(m, entry, buffer);
                    continue;
                }
            }
            for (ModelPart.Vertex vertex : quad.vertices()) {
                Vector3f pos = ((VertexAccessor) (Object) vertex).getPos();
                posMat.transformPosition(pos.x() / 16.0F, pos.y() / 16.0F, pos.z() / 16.0F, position);
                buffer.vertex(
                        position.x, position.y, position.z,
                        color, vertex.u(), vertex.v(), overlay, light,
                        normal.x, normal.y, normal.z
                );
            }
        }
    }

    private void drawFace(FTModel model, MatrixStack.Entry entry, VertexConsumer buffer) {
        model.update(MCFTClient.fps);
        Matrix4f posMat = entry.getPositionMatrix();
        EyeTrackingRect r = model.eyeR;
        EyeTrackingRect l = model.eyeL;
        MouthTrackingRect m = model.mouth;
        if (model.isFlat) {
            drawFace(posMat, buffer, 0, 0, r.x, r.y);
            drawFace(posMat, buffer, r.x, 0, r.w, r.y - r.ih);
            drawFace(posMat, buffer, r.x + r.w, 0, l.x - r.x - r.w, 8);
            drawFace(posMat, buffer, l.x, 0, l.w, l.y - l.ih);
            drawFace(posMat, buffer, l.x + l.w, 0, 8 - l.x - l.w, l.y);
            drawFace(posMat, buffer, 0, r.y, 8, 8 - r.y);
            drawEyeFlat(entry, buffer, r, m);
            drawEyeFlat(entry, buffer, l, m);
            return;
        }
        drawFace(posMat, buffer, 0, 0, r.x, r.y);
        drawFace(posMat, buffer, r.x, 0, r.w, r.y - r.ih);
        drawFace(posMat, buffer, r.x + r.w, 0, l.x - r.x - r.w, m.y - m.h);
        drawFace(posMat, buffer, l.x, 0, l.w, l.y - l.ih);
        drawFace(posMat, buffer, l.x + l.w, 0, 8 - l.x - l.w, l.y);
        drawFace(posMat, buffer, 0, r.y, m.x, 8 - r.y);
        drawFace(posMat, buffer, m.x + m.w, l.y, 8 - m.x - m.w, 8 - l.y);
        drawFace(posMat, buffer, m.x, m.y, m.w, 8 - m.y);
        drawEye(entry, buffer, r);
        drawEye(entry, buffer, l);
        drawCube(entry, buffer, m.x - 4, m.y - m.h - 8, -3, m.u1, m.v1, m.x + m.w - 4, m.y - 8, -4, m.u2, m.v2, true, true, false);
    }

    private void drawEye(MatrixStack.Entry entry, VertexConsumer buffer, EyeTrackingRect e) {
        Matrix4f posMat = entry.getPositionMatrix();
        drawCube(entry, buffer, e.x - 4, e.y - e.ih - 8, -2, e.inner.u1, e.inner.v2, e.x + e.w - 4, e.y - 8, -4f, e.inner.u2, e.inner.v1, true, true, true);
        drawQuad(posMat, buffer, e.x - 4, e.y - 8, e.inner.u1, e.inner.v2, e.x + e.w - 4, e.y - e.ih - 8, e.inner.u2, e.inner.v1, -3.9f);
        drawCube(entry, buffer, e.x + (e.w - e.ball.w) / 2 + e.ball.x - 4, e.y - (e.ih + e.ball.h) / 2 + e.ball.y - 8, -3, e.ball.u1, e.ball.v1, e.x + (e.w + e.ball.w) / 2 + e.ball.x - 4, e.y - (e.ih - e.ball.h) / 2 + e.ball.y - 8, -3.95f, e.ball.u2, e.ball.v2, false, false, true);
        drawQuad(posMat, buffer, e.x - 4, e.y - e.ih - 8, e.lid.u1, e.lid.v2, e.x + e.w - 4, e.y - e.h - 8, e.lid.u2, e.lid.v1);
        drawCube(entry, buffer, e.x - 4, e.y - e.h - 0.1f - 8, -4, e.lid.u1, e.lid.v2 - 0.0001f, e.x + e.w - 4, e.y - e.h - 8, -4.1f, e.lid.u2, e.lid.v2, false, false, true);
    }

    private void drawEyeFlat(MatrixStack.Entry entry, VertexConsumer buffer, EyeTrackingRect e, MouthTrackingRect brow) {
        Matrix4f posMat = entry.getPositionMatrix();
        drawQuad(posMat, buffer, e.x - 4, e.y - 8, e.inner.u1, e.inner.v2, e.x + e.w - 4, e.y - e.ih - 8, e.inner.u2, e.inner.v1, -3.998f);
        drawQuad(posMat, buffer, e.x + (e.w - e.ball.w) / 2 + e.ball.x - 4, e.y - (e.ih - e.ball.h) / 2 + e.ball.y - 8, e.ball.u1, e.ball.v2, e.x + (e.w + e.ball.w) / 2 + e.ball.x - 4, e.y - (e.ih + e.ball.h) / 2 + e.ball.y - 8, e.ball.u2, e.ball.v1, -3.999f);
        drawQuad(posMat, buffer, e.x - 4, e.y - e.h - 8, e.lid.u1, MathHelper.lerp(1 - e.h / e.ih, e.lid.v1, e.lid.v2), e.x + e.w - 4, e.y - e.ih - 8, e.lid.u2, e.lid.v1, -4.001f);
        drawQuad(posMat, buffer, e.x - 4, e.y - e.h - brow.h - 1 - 8, brow.u1, brow.v2, e.x + e.w - 4, e.y - e.h - brow.h - 2 - 8, brow.u2, brow.v1, -4.002f);
    }

    private void drawCube(MatrixStack.Entry entry, VertexConsumer buffer, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, boolean inner, boolean skipFront, boolean skipBack) {
        Matrix4f posMat = entry.getPositionMatrix();
        if (!skipFront) drawQuad(posMat, buffer, x1, y1, z2, u1, v1, x1, y2, z2, u1, v2, x2, y2, z2, u2, v2, x2, y1, z2, u2, v1, entry.transformNormal((inner ? Direction.SOUTH : Direction.NORTH).getUnitVector(), tmp));
        if (!skipBack) drawQuad(posMat, buffer, x1, y1, z1, u1, v1, x1, y2, z1, u1, v2, x2, y2, z1, u2, v2, x2, y1, z1, u2, v1, entry.transformNormal((inner ? Direction.NORTH : Direction.SOUTH).getUnitVector(), tmp));
        drawQuad(posMat, buffer, x1, y1, z1, u1, v1, x1, y1, z2, u2, v1, x1, y2, z2, u2, v2, x1, y2, z1, u1, v2, entry.transformNormal((inner ? Direction.EAST : Direction.WEST).getUnitVector(), tmp));
        drawQuad(posMat, buffer, x2, y1, z1, u1, v1, x2, y1, z2, u2, v1, x2, y2, z2, u2, v2, x2, y2, z1, u1, v2, entry.transformNormal((inner ? Direction.WEST : Direction.EAST).getUnitVector(), tmp));
        drawQuad(posMat, buffer, x1, y2, z1, u1, v1, x1, y2, z2, u1, v2, x2, y2, z2, u2, v2, x2, y2, z1, u2, v1, entry.transformNormal((inner ? Direction.DOWN : Direction.UP).getUnitVector(), tmp));
        drawQuad(posMat, buffer, x1, y1, z1, u1, v1, x2, y1, z1, u2, v1, x2, y1, z2, u2, v2, x1, y1, z2, u1, v2, entry.transformNormal((inner ? Direction.UP : Direction.DOWN).getUnitVector(), tmp));
    }

    private void drawFace(Matrix4f posMat, VertexConsumer buffer, float x, float y, float w, float h) {
        drawQuad(posMat, buffer, x - 4, y - 8, 0.125f + x / 64, 0.125f + y / 64, x + w - 4, y + h - 8, 0.125f + (x + w) / 64, 0.125f + (y + h) / 64);
    }

    private void drawQuad(Matrix4f posMat, VertexConsumer buffer, float x1, float y1, float u1, float v1, float x2, float y2, float u2, float v2) {
        drawQuad(posMat, buffer, x1, y1, u1, v1, x1, y2, u1, v2, x2, y2, u2, v2, x2, y1, u2, v1);
    }

    private void drawQuad(Matrix4f posMat, VertexConsumer buffer, float x1, float y1, float u1, float v1, float x2, float y2, float u2, float v2, float z) {
        drawQuad(posMat, buffer, x1, y1, z, u1, v1, x2, y1, z, u2, v1, x2, y2, z, u2, v2, x1, y2, z, u1, v2, normal);
    }

    private void drawQuad(
            Matrix4f posMat, VertexConsumer buffer,
            float x1, float y1, float u1, float v1, float x2, float y2, float u2, float v2,
            float x3, float y3, float u3, float v3, float x4, float y4, float u4, float v4
    ) {
        drawQuad(posMat, buffer, x1, y1, -4, u1, v1, x2, y2, -4, u2, v2, x3, y3, -4, u3, v3, x4, y4, -4, u4, v4, normal);
    }

    private void drawQuad(
            Matrix4f posMat, VertexConsumer buffer,
            float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3, float x4, float y4, float z4, float u4, float v4,
            Vector3f normal
    ) {
        float nx = normal.x;
        float ny = normal.y;
        float nz = normal.z;
        posMat.transformPosition(x1 / 16.0F, y1 / 16.0F, z1 / 16.0F, position);
        buffer.vertex(position.x, position.y, position.z, color, u1, v1, overlay, light, nx, ny, nz);
        posMat.transformPosition(x2 / 16.0F, y2 / 16.0F, z2 / 16.0F, position);
        buffer.vertex(position.x, position.y, position.z, color, u2, v2, overlay, light, nx, ny, nz);
        posMat.transformPosition(x3 / 16.0F, y3 / 16.0F, z3 / 16.0F, position);
        buffer.vertex(position.x, position.y, position.z, color, u3, v3, overlay, light, nx, ny, nz);
        posMat.transformPosition(x4 / 16.0F, y4 / 16.0F, z4 / 16.0F, position);
        buffer.vertex(position.x, position.y, position.z, color, u4, v4, overlay, light, nx, ny, nz);
    }

    public static FTCuboid newInstance(ModelPart.Cuboid obj) {
        CuboidAccessor old = (CuboidAccessor) obj;
        CuboidAccessor now;
        try {
            now = (CuboidAccessor) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        now.setMinX(old.getMinX());
        now.setMinY(old.getMinY());
        now.setMinZ(old.getMinZ());
        now.setMaxX(old.getMaxX());
        now.setMaxY(old.getMaxY());
        now.setMaxZ(old.getMaxZ());
        now.setSides(old.getSides());
        return (FTCuboid) now;
    }
}
