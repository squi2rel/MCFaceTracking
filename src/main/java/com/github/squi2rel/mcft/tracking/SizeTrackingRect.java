package com.github.squi2rel.mcft.tracking;

import io.netty.buffer.ByteBuf;

public class SizeTrackingRect extends TrackingRect {

    @Override
    public void readSync(ByteBuf buf) {
        w = buf.readFloat();
        h = buf.readFloat();
    }

    @Override
    public void writeSync(ByteBuf buf) {
        buf.writeFloat(w);
        buf.writeFloat(h);
    }
}
