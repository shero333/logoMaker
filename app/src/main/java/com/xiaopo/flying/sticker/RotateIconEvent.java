package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

public class RotateIconEvent implements StickerIconEvent{
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.rotateCurrentSticker(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        if (stickerView.getOnStickerOperationListener() != null) {
            assert stickerView.getCurrentSticker() != null;
            stickerView.getOnStickerOperationListener()
                    .onStickerZoomFinished(stickerView.getCurrentSticker());
        }
    }
}
