package com.xiaopo.flying.sticker.stickers;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.xiaopo.flying.sticker.Sticker;

/**
 * @author wupanjie
 */
public class ElementSticker extends Sticker {

  private Drawable drawable;
  private Rect realBounds;

  public ElementSticker(Drawable drawable) {
    this.drawable = drawable;
    realBounds = new Rect(0, 0, getWidth(), getHeight());
  }

  public ElementSticker() {
  }

  @NonNull
  @Override public Drawable getDrawable() {
    return drawable;
  }

  @Override public ElementSticker setDrawable(@NonNull Drawable drawable) {
    this.drawable = drawable;
    return this;
  }

  @Override public void draw(@NonNull Canvas canvas) {
    canvas.save();
    canvas.concat(getMatrix());
    drawable.setBounds(realBounds);
    drawable.draw(canvas);
    canvas.restore();
  }

  @NonNull @Override public ElementSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    drawable.setAlpha(alpha);
    return this;
  }

  @Override public int getWidth() {
    return drawable.getIntrinsicWidth();
  }

  @Override public int getHeight() {
    return drawable.getIntrinsicHeight();
  }

  @Override public void release() {
    super.release();
    if (drawable != null) {
      drawable = null;
    }
  }
}
