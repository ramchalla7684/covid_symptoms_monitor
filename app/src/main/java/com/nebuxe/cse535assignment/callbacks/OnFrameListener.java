package com.nebuxe.cse535assignment.callbacks;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public interface OnFrameListener {
    void onNewFrameAvailable(ByteBuffer[] byteBuffers);
    void onNewFrameAvailable(Bitmap bitmap);
}
