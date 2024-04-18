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
    public final int song;

    public BallDesign(Resources resources, int id, int drawable, float radiusMultiplier, String emoji, int minScore, int song) {

        this.id = id;
        this.drawable = drawable;
        this.emoji = emoji;
        this.minScore = minScore;
        this.radiusMultiplier = radiusMultiplier;
        this.song = song;
        if (this.drawable > 0) {
            this.bitmap = BitmapFactory.decodeResource(resources, drawable);
            this.bitmapRect = new Rect(0, 0, this.bitmap.getWidth(), this.bitmap.getHeight());
        } else {
            this.bitmap = null;
            this.bitmapRect = null;
        }
    }

    public BallDesign(Resources resources, int id, int drawable, float radiusMultiplier, int minScore) {
        this(resources, id, drawable, radiusMultiplier, null, minScore, -1);
    }

    public BallDesign(Resources resources, int id, int drawable, float radiusMultiplier, int minScore, int song) {
        this(resources, id, drawable, radiusMultiplier, null, minScore, song);
    }

    public BallDesign(Resources resources, int id, String emoji, float radiusMultiplier, int minScore) {
        this(resources, id, -1, radiusMultiplier, emoji, minScore, -1);
    }
}

