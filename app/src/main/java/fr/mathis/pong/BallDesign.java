package fr.mathis.pong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import androidx.core.content.res.ResourcesCompat;

public class BallDesign {

    public final int id;
    public final int drawable;
    public final Rect bitmapRect;
    public final Bitmap bitmap;
    public final float radiusMultiplier;
    public final String emoji;
    public final int minScore;

    public BallDesign(Resources resources, int id, int drawable, float radiusMultiplier, String emoji, int minScore) {

        this.id = id;
        this.drawable = drawable;
        this.emoji = emoji;
        this.minScore = minScore;
        this.radiusMultiplier = radiusMultiplier;
        if (this.drawable > 0) {
            this.bitmap = BitmapFactory.decodeResource(resources, drawable);
            this.bitmapRect = new Rect(0, 0, this.bitmap.getWidth(), this.bitmap.getHeight());
        } else {
            this.bitmap = null;
            this.bitmapRect = null;
        }
    }
}

