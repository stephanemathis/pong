package fr.mathis.pong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;
import java.util.Vector;

public class PongView extends View {

    // Idée : bretzel, fantome snap, tête de gaelle, stéphane ou thérèse
    TimerIntegration _timer;
    Random _random;
    Vector<Ball> _balls;
    Bar _bar;


    Paint _ballPaint;
    Paint _barPaint;
    Paint _emojiPaint;
    int _viewHeight;
    int _viewWidth;

    PongListener _listener;

    int _score;
    boolean _easyMode;

    BallDesign _ballDesign;
    Rect _ballRect;


    public PongView(Context context) {
        super(context);

        this.init(context, null, 0);
    }

    public PongView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.init(context, attrs, 0);
    }

    public PongView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.init(context, attrs, defStyleAttr);
    }

    public PongView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        _timer = new TimerIntegration();
        _random = new Random();
        _balls = new Vector<>();
        _listener = null;
        _score = 0;
        _ballDesign = DataManager.getCurrentBallDesign(context);
        _ballRect = new Rect(0, 0, 0, 0);
        _easyMode = false;

        _ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _ballPaint.setStyle(Paint.Style.STROKE);
        _ballPaint.setAntiAlias(true);
        _ballPaint.setStrokeWidth(convertDpToPixel(2));
        _ballPaint.setColor(Color.WHITE);

        _barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _barPaint.setStyle(Paint.Style.FILL);
        _barPaint.setDither(true);
        _barPaint.setStrokeJoin(Paint.Join.ROUND);
        _barPaint.setStrokeCap(Paint.Cap.ROUND);
        _barPaint.setAntiAlias(true);

        _emojiPaint = new Paint();
        _emojiPaint.setTextSize(convertDpToPixel(48));
        _emojiPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initData() {

        _balls.clear();
        _score = 0;
        _ballDesign = DataManager.getCurrentBallDesign(this.getContext());

        _bar = new Bar(_viewWidth / 2f, _viewHeight - convertDpToPixel(24), _viewWidth / (_easyMode ? 2f : 4f), convertDpToPixel(36));

        int angle = 180 + 45 + 15 - 15 + (int) (_random.nextFloat() * 15);

        if (_random.nextBoolean()) angle += 90;

        _balls.add(new Ball(_viewWidth / 2f, _viewHeight / 2f, convertDpToPixel(24), angle, convertDpToPixel(700), 0));

        _timer.getDeltaTime();
    }

    private void updatePhysics() {
        float deltaTime = _timer.getDeltaTime();

        // On va découper l'update de 16ms en paquet de 4ms
        float steps = 0.004f;

        int packetsCount = (int) (deltaTime / steps);
        float lastPacketDuration = deltaTime % steps;

        boolean hasLastPacket = false;
        if (lastPacketDuration > 0f) {
            packetsCount++;
            hasLastPacket = true;
        }

        for (int p = 0; p < packetsCount; p++) {
            float deltaTimeToTreat = p == packetsCount - 1 && hasLastPacket ? lastPacketDuration : steps;

            for (int i = 0; i < _balls.size(); i++) {

                if (_balls.size() < i) continue;
                Ball ball = _balls.get(i);

                ball.currentRotation += ball.rotationPerSecond * deltaTimeToTreat;

                //Détection de la barre
                if (intersects(deltaTimeToTreat, ball, _bar) && ball.lost == false) {
                    ball.dy = -ball.dy;
                    this.onTouchBar();
                } else {
                    // Sous la barre
                    if (ball.y + ball.radius + ball.dy * ball.speedFactor * deltaTimeToTreat > _bar.y - _bar.height / 2f && ball.lost == false) {
                        // lost
                        ball.lost = true;
                        this.vibrate(200);
                        this.onTouchBottom();
                    }

                    // Bas de l'écran
                    if (ball.y + ball.radius + ball.dy * ball.speedFactor * deltaTimeToTreat > _viewHeight) {
                        ball.dy = -ball.dy;
                    }
                }

                // Détection gauche/droite
                if (ball.x + ball.radius + ball.dx * ball.speedFactor * deltaTimeToTreat > _viewWidth || ball.x - ball.radius + ball.dx * ball.speedFactor * deltaTime < 0) {
                    ball.dx = -ball.dx;
                }


                // Haut
                if (ball.y - ball.radius + ball.dy * ball.speedFactor * deltaTimeToTreat < 0) {
                    ball.dy = -ball.dy;
                }

                ball.x = ball.x + ball.dx * ball.speedFactor * deltaTimeToTreat;
                ball.y = ball.y + ball.dy * ball.speedFactor * deltaTimeToTreat;
            }
        }
    }

    private boolean intersects(float deltaTime, Ball circle, Bar rect) {

        if (circle.dy < 0) return false;

        float xMiddle = circle.x + circle.dx * circle.speedFactor * deltaTime;
        float yMiddle = circle.y + circle.radius + circle.dy * circle.speedFactor * deltaTime;

        if (rect.x - rect.width / 2f < xMiddle && xMiddle < rect.x + rect.width / 2f) {
            if (yMiddle > rect.y - rect.height / 2f) {
                return true;
            }
        }

        return false;

        /*float x = Math.abs(circle.x + circle.dx * circle.speedFactor * deltaTime - rect.x);
        float y = Math.abs(circle.y + circle.dy * circle.speedFactor * deltaTime - rect.y);

        if (x > (rect.width / 2 + circle.radius)) {
            return false;
        }
        if (y > (rect.height / 2 + circle.radius)) {
            return false;
        }

        if (x <= (rect.width / 2)) {
            return true;
        }
        if (y <= (rect.height / 2)) {
            return true;
        }
        double cornerDistance = Math.pow(x - rect.width / 2, 2) + Math.pow(y - rect.height / 2, 2);

        return (cornerDistance <= Math.pow(circle.radius, 2));*/
    }

    private void onTouchBar() {
        this._score += 1;
        this.informScoreChanged();

        this.vibrate(50);

        for (Ball point : _balls) {
            point.speedFactor = this.properSpeed(point);
        }
    }

    private void vibrate(int duration) {
        Vibrator v = (Vibrator) this.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(duration);
        }
    }

    private void onTouchBottom() {
        for (Ball point : _balls) {
            point.speedFactor = 1f;//this.properSpeed(point);
        }

        Ball firstBall = _balls.get(0);

        for (int i = 0; i < this._score - 1; i++) {
            int angle = 180 + 20 + (int) (_random.nextFloat() * 160);

            Ball newBall = new Ball(firstBall, angle);
            newBall.rotationPerSecond = 0;
            _balls.add(newBall);
        }

        _bar.height = 0;
        _bar.width = 0;

        firstBall.showBoundaries = true;

        if (_listener != null) _listener.onLost(_ballDesign.id, this._score);
    }

    private float properSpeed(Ball _ball) {
        return Math.max(1f, (float) Math.sqrt((double) this._score / 7f) + 0.3f);
        //return 1f + (this._score / 10f) * 0.2f;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (_bar != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                _bar.x = event.getX();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                _bar.x = event.getX();
            }

            if (_bar.x - _bar.width / 2f < 0) _bar.x = _bar.width / 2f;
            else if (_bar.x + _bar.width / 2f > _viewWidth) _bar.x = _viewWidth - _bar.width / 2f;
        }
        return true;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        if (height != _viewHeight || width != _viewWidth) {
            boolean isZero = _viewHeight == 0;
            _viewHeight = getHeight();
            _viewWidth = getWidth();
            if (!isZero) initData();
        }

        updatePhysics();

        for (Ball ball : _balls) {

            if (_ballDesign.emoji != null) {
                float ascent = Math.abs(_emojiPaint.ascent());
                float descent = Math.abs(_emojiPaint.descent());
                float halfHeight = (ascent + descent) / 2.0f;
                canvas.rotate(ball.currentRotation, ball.x, ball.y);
                canvas.drawText(_ballDesign.emoji, ball.x, ball.y + halfHeight - descent, _emojiPaint);
                canvas.rotate(-ball.currentRotation, ball.x, ball.y);
            } else {
                canvas.rotate(ball.currentRotation, ball.x, ball.y);
                this._ballRect.set((int) ball.x - (int) (ball.radius * _ballDesign.radiusMultiplier), (int) ball.y - (int) (ball.radius * _ballDesign.radiusMultiplier), (int) ball.x + (int) (ball.radius * _ballDesign.radiusMultiplier), (int) ball.y + (int) (ball.radius * _ballDesign.radiusMultiplier));
                canvas.drawBitmap(_ballDesign.bitmap, _ballDesign.bitmapRect, this._ballRect, _emojiPaint);
                canvas.rotate(-ball.currentRotation, ball.x, ball.y);
            }

            if (ball.showBoundaries)
                canvas.drawCircle(ball.x, ball.y, ball.radius, _ballPaint);
        }

        if (_bar != null) {
            _barPaint.setStrokeWidth(_bar.height);
            _barPaint.setColor(Color.DKGRAY);
            canvas.drawLine(_bar.x - _bar.width / 2f + _bar.height / 2f, _bar.y, _bar.x + _bar.width / 2f - _bar.height / 2f, _bar.y, _barPaint);

          /*  _barPaint.setStrokeWidth(4);
            _barPaint.setColor(Color.RED);
            canvas.drawLine(_bar.x - _bar.width / 2f, _bar.y, _bar.x + _bar.width / 2f, _bar.y, _barPaint);*/
        }

        if (!_balls.isEmpty()) invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = convertDpToPixel(100 * 3);
        int desiredHeight = convertDpToPixel(200 * 3);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    public void start() {
        this.initData();
        this.informScoreChanged();
        invalidate();
    }

    public void stop() {
        _balls.stream().forEach(b -> b.lost = true);
        this.onTouchBottom();
    }

    public void setListener(PongListener listener) {
        this._listener = listener;
    }

    public void setDifficulty(boolean easyMode) {
        _easyMode = easyMode;
    }

    private void informScoreChanged() {
        if (this._listener != null) this._listener.onScoreChanged(this._score);
    }

    class Bar {
        public float x;
        public float y;
        public float width;
        public float height;

        public Bar(float _x, float _y, float _width, float _height) {
            this.x = _x;
            this.y = _y;
            this.width = _width;
            this.height = _height;
        }
    }

    class Ball {

        public float x;
        public float y;
        public float dx;
        public float dy;
        public float radius;
        public float speedFactor;
        public int rotationPerSecond;
        public float currentRotation;
        public boolean lost;
        public float dpPerSecond;
        public boolean showBoundaries;

        public Ball(float _x, float _y, float _radius, int _direction, float _dpPerSecond, int _generatedIndex) {
            this.x = _x;
            this.y = _y;
            this.radius = _radius;
            double angle = Math.toRadians(_direction);

            this.dx = (float) Math.cos(angle) * _dpPerSecond;
            this.dy = (float) Math.sin(angle) * _dpPerSecond;

            this.speedFactor = 1f;
            this.rotationPerSecond = (_random.nextBoolean() ? 1 : -1) * (360 - 90);
            this.currentRotation = 0f;
            this.lost = false;
            this.dpPerSecond = _dpPerSecond;
            this.showBoundaries = false;
        }

        public Ball(Ball _source, int _direction) {
            this.x = _source.x;
            this.y = _source.y;
            this.radius = _source.radius;
            double angle = Math.toRadians(_direction);

            this.dx = (float) Math.cos(angle) * _source.dpPerSecond;
            this.dy = (float) Math.sin(angle) * _source.dpPerSecond;

            this.speedFactor = _source.speedFactor;
            this.rotationPerSecond = _source.rotationPerSecond;
            this.currentRotation = _source.currentRotation;
            this.lost = _source.lost;
            this.dpPerSecond = _source.dpPerSecond;
            this.showBoundaries = _source.showBoundaries;
        }
    }

    class TimerIntegration {
        private long previousTime = -1L;

        public void reset() {
            previousTime = -1L;
        }

        public float getDeltaTime() {
            if (previousTime == -1L) previousTime = System.nanoTime();

            long currentTime = System.nanoTime();
            long dt = (currentTime - previousTime) / 1000000;
            previousTime = currentTime;
            return (float) dt / 1000;
        }

        public long getTotalTimeRunning(long startTime) {
            long currentTime = System.currentTimeMillis();
            return (currentTime - startTime);
        }

    }

    public static int convertDpToPixel(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertPixelToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public interface PongListener {
        void onScoreChanged(int score);

        void onLost(int ballId, int finalScore);
    }
/*
    @ColorInt
    int darkenColor(@ColorInt int color, float ratio) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= ratio;
        return Color.HSVToColor(hsv);
    }*/
}
