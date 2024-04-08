package fr.mathis.pong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
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

        _ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _ballPaint.setStyle(Paint.Style.FILL);
        _ballPaint.setAntiAlias(true);
        _ballPaint.setColor(Color.BLUE);

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
        _bar = new Bar(_viewWidth / 2f, _viewHeight - convertDpToPixel(24), _viewWidth / 4f, convertDpToPixel(36));

        int angle = 180 + 45 + 15 - 15 + (int) (_random.nextFloat() * 15);

        if (_random.nextBoolean())
            angle += 90;

        _balls.add(new Ball(
                _viewWidth / 2f,
                _viewHeight / 2f,
                convertDpToPixel(24),
                angle,
                convertDpToPixel(700),
                0)
        );
    }

    private void updatePhysics() {
        float deltaTime = _timer.getDeltaTime();

        for (Ball ball : _balls) {

            ball.currentRotation += ball.rotationPerSecond * deltaTime;

            //Détection de la barre
            if (intersects(deltaTime, ball, _bar) && ball.lost == false) {
                ball.dy = -ball.dy;
                this.onTouchBar();
            } else {
                // Sous la barre
                if (ball.y + ball.radius + ball.dy * ball.speedFactor * deltaTime > _bar.y - _bar.height / 2f) {
                    // lost
                    ball.lost = true;
                    this.onTouchBottom();
                }
                // Bas de l'écran
                if (ball.y + ball.radius + ball.dy * ball.speedFactor * deltaTime > _viewHeight) {
                    ball.dy = -ball.dy;

                    ball.dy = 0;
                    ball.dx = 0;
                    ball.y = _viewHeight - ball.radius;
                    ball.rotationPerSecond = 0;
                }
            }

            // Détection gauche/droite
            if (ball.x + ball.radius + ball.dx * ball.speedFactor * deltaTime > _viewWidth || ball.x - ball.radius + ball.dx * ball.speedFactor * deltaTime < 0) {
                ball.dx = -ball.dx;
            }


            // Haut
            if (ball.y - ball.radius + ball.dy * ball.speedFactor * deltaTime < 0) {
                ball.dy = -ball.dy;
            }

            ball.x = ball.x + ball.dx * ball.speedFactor * deltaTime;
            ball.y = ball.y + ball.dy * ball.speedFactor * deltaTime;
        }
    }

    private boolean intersects(float deltaTime, Ball circle, Bar rect) {

        if (circle.dy < 0)
            return false;

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

        for (Ball point : _balls) {
            point.speedFactor = this.properSpeed(point);
        }
    }

    private void onTouchBottom() {
        this._score -= 5;
        if (this._score < 0)
            this._score = 0;
        this.informScoreChanged();

        for (Ball point : _balls) {
            point.speedFactor = this.properSpeed(point);
        }

        if (_listener != null)
            _listener.onLost();
    }

    private float properSpeed(Ball _ball) {
        return 1f + (this._score / 10f) * 0.2f;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _bar.x = event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            _bar.x = event.getX();
        }

        if (_bar.x - _bar.width / 2f < 0)
            _bar.x = _bar.width / 2f;
        else if (_bar.x + _bar.width / 2f > _viewWidth)
            _bar.x = _viewWidth - _bar.width / 2f;

        return true;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        if (height != _viewHeight || width != _viewWidth) {
            _viewHeight = getHeight();
            _viewWidth = getWidth();
            initData();
        }

        if (_balls.isEmpty())
            initData();

        updatePhysics();

        for (Ball ball : _balls) {
            canvas.drawCircle(ball.x, ball.y, ball.radius, _ballPaint);

            final String EMOJI = "❤\uFE0F";//"\uD83C\uDF0D"; // the Earth Globe (Europe, Africa) emoji - should never be transparent in the center
            float ascent = Math.abs(_emojiPaint.ascent());
            float descent = Math.abs(_emojiPaint.descent());
            float halfHeight = (ascent + descent) / 2.0f;
            canvas.rotate(ball.currentRotation, ball.x, ball.y);
            canvas.drawText(EMOJI, ball.x, ball.y + halfHeight - descent, _emojiPaint);
            canvas.rotate(-ball.currentRotation, ball.x, ball.y);
        }

        _barPaint.setStrokeWidth(_bar.height);
        _barPaint.setColor(Color.DKGRAY);
        canvas.drawLine(_bar.x - _bar.width / 2f + _bar.height / 4f, _bar.y, _bar.x + _bar.width / 2f - _bar.height / 4f, _bar.y, _barPaint);

        _barPaint.setStrokeWidth(4);
        _barPaint.setColor(Color.RED);
        canvas.drawLine(_bar.x - _bar.width / 2f, _bar.y, _bar.x + _bar.width / 2f, _bar.y, _barPaint);

        if (!_balls.isEmpty())
            invalidate();
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
        _balls.clear();
        _score = 0;

        this.informScoreChanged();
        invalidate();
    }

    public void setListener(PongListener listener) {
        this._listener = listener;
    }

    private void informScoreChanged() {
        if (this._listener != null)
            this._listener.onScoreChanged(this._score);
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

        public Ball(float _x, float _y, float _radius, int _direction, float _dpPerSecond, int _generatedIndex) {
            this.x = _x;
            this.y = _y;
            this.radius = _radius;
            double angle = Math.toRadians(_direction);

            this.dx = (float) Math.cos(angle) * _dpPerSecond;
            this.dy = (float) Math.sin(angle) * _dpPerSecond;

            this.speedFactor = 1f;
            this.rotationPerSecond = 360;
            this.currentRotation = 0f;
            this.lost = false;
        }
    }

    class TimerIntegration {
        private long previousTime = -1L;

        public void reset() {
            previousTime = -1L;
        }

        public float getDeltaTime() {
            if (previousTime == -1L)
                previousTime = System.nanoTime();

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

        void onLost();
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