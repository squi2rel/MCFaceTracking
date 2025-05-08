package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FTModel {
    public static FTModel model;
    public EyeTrackingRect eyeR = new EyeTrackingRect(0, 0, 0, 0);
    public EyeTrackingRect eyeL = new EyeTrackingRect(0, 0, 0, 0);
    public MouthTrackingRect mouth = new MouthTrackingRect(0, 0, 0, 0);
    public boolean isFlat = false;
    public transient boolean enabled = false;
    public transient long lastReceived = 0;

    public FTModel() {
    }

    public FTModel(EyeTrackingRect eyeR, EyeTrackingRect eyeL, MouthTrackingRect mouth, boolean isFlat) {
        this.eyeR = eyeR;
        this.eyeL = eyeL;
        this.mouth = mouth;
        this.isFlat = isFlat;
    }

    public void update() {
        eyeR.update();
        eyeL.update();
        if (!isFlat) mouth.update();
    }

    public boolean active() {
        return System.currentTimeMillis() - lastReceived < 3000;
    }

    public void readSync(byte[] data) {
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        eyeR.readSync(buf);
        eyeL.readSync(buf);
        mouth.readSync(buf);
        if (buf.readableBytes() != 0) throw new IllegalArgumentException("buffer remaining " + buf.readableBytes() + " bytes");
        lastReceived = System.currentTimeMillis();
    }

    public void validate(boolean init) {
        eyeR.validate(init);
        eyeL.validate(init);
        mouth.validate(init);
    }
}
