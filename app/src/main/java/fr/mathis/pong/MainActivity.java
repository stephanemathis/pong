package fr.mathis.pong;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PongView.PongListener {

    ConstraintLayout container;
    PongView _pongView;
    TextView _score;
    Button _replay;
    Switch _swDifficulty;
    ImageView _ivSettings;
    CardView _cvSettings;
    RecyclerView _ballRecyclerView;
    BallAdapter _adapter;
    int _currentBackgroundColor;
    BallDesign _currentDesign;
    MediaPlayer _mediaPlayer;
    int _currentSong;
    OnBackPressedCallback _backCallback;
    StateView _svLogo;
    boolean _uiLocked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.main);
        _score = findViewById(R.id.score);
        _pongView = findViewById(R.id.pongGame);
        _replay = findViewById(R.id.btnReplay);
        _ivSettings = findViewById(R.id.ivSettings);
        _cvSettings = findViewById(R.id.cvSettings);
        _ballRecyclerView = findViewById(R.id.rvBalls);
        _swDifficulty = findViewById(R.id.swDifficulty);
        _svLogo = findViewById(R.id.svLogo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.insideContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
            newLayoutParams.topMargin = systemBars.top;
            newLayoutParams.leftMargin = systemBars.left;
            newLayoutParams.rightMargin = systemBars.right;
            newLayoutParams.bottomMargin = systemBars.bottom;
            v.setLayoutParams(newLayoutParams);

            return insets;
        });

        _swDifficulty.setChecked(DataManager.isEasyMode(this));

        _swDifficulty.setOnCheckedChangeListener((button, value) -> {
            DataManager.setEasyMode(this, value);
            _pongView.setDifficulty(value);
        });

        _replay.setOnClickListener(v -> {
            this.onReplayClicked();
        });

        _ivSettings.setOnClickListener(v -> {
            this.onSettingClicked();
        });

        _pongView.setListener(this);
        _pongView.setDifficulty(DataManager.isEasyMode(this));

        _adapter = new BallAdapter();
        _ballRecyclerView.setAdapter(_adapter);
        _ballRecyclerView.setNestedScrollingEnabled(true);

        _currentDesign = DataManager.getCurrentBallDesign(this);

        _backCallback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                _pongView.stop();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, _backCallback);

        _backCallback.setEnabled(false);

        _svLogo.setSvgResource(R.raw.logo3coeur);
        _svLogo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((StateView) v).reset();
                ((StateView) v).reveal(v.getRootView(), 0);
            }
        });

        _ivSettings.setVisibility(View.INVISIBLE);
        _replay.setVisibility(View.INVISIBLE);
        _svLogo.reveal(container, 0);
        _uiLocked = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                _ivSettings.setVisibility(View.VISIBLE);
                _replay.setVisibility(View.VISIBLE);
                _uiLocked = false;
            }
        }, 3000);   //5 seconds
    }

    boolean paused = false;

    @Override
    protected void onPause() {
        super.onPause();

        if (_mediaPlayer != null) {
            _mediaPlayer.stop();
        }

        this.paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.paused) {
            this.paused = false;
            this.onReplayClicked();
            if (_mediaPlayer != null) {
                _mediaPlayer.start();
            }
        }
    }

    @Override
    public void onScoreChanged(int score) {
        if (this._score != null) this._score.setText("" + score);

        this.updateBackgroundColor(score);
    }

    @Override
    public void onLost(int ballId, int finalScore) {
        _replay.setText(R.string.replay);
        _replay.setVisibility(View.VISIBLE);
        _ivSettings.setVisibility(View.VISIBLE);
        _backCallback.setEnabled(false);

        DataManager.saveScore(getBaseContext(), ballId, finalScore);
        DataManager.saveUnlocked(getBaseContext(), ballId, true);


        int i = 0;

        for (; i < _adapter._designs.size(); i++)
            if (_adapter._designs.get(i).id == ballId) break;


        _ballRecyclerView.getAdapter().notifyItemChanged(i);

    }

    private void updateBackgroundColor(int score) {
        int newBackgroundColor = DataManager.getBackgroundColor(score, getResources());

        if (_currentBackgroundColor != newBackgroundColor) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), _currentBackgroundColor, newBackgroundColor);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(animator -> container.setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimation.start();

            _currentBackgroundColor = newBackgroundColor;
        }
    }

    private void onReplayClicked() {

        if(this._uiLocked)
            return;

        _replay.setVisibility(View.GONE);
        _ivSettings.setVisibility(View.GONE);
        _cvSettings.setVisibility(View.GONE);
        _svLogo.setVisibility(View.GONE);
        _pongView.start();
        _backCallback.setEnabled(true);

        if (_currentSong != _currentDesign.song) {
            if (_mediaPlayer != null) {
                _mediaPlayer.stop();
                _mediaPlayer.release();
                _mediaPlayer = null;
            }

            if (_currentDesign.song > 0) {
                try {
                    _mediaPlayer = MediaPlayer.create(this, _currentDesign.song);
                    _currentSong = _currentDesign.song;

                    _mediaPlayer.setVolume(1f, 1f);
                    _mediaPlayer.setLooping(true);
                    _mediaPlayer.start();


                } catch (IllegalStateException e) {
                    _mediaPlayer = null;
                }
            }
        }

    }

    private void onSettingClicked() {
        if(this._uiLocked)
            return;

        _cvSettings.setVisibility(_cvSettings.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }


    public void onBallSelected(BallDesign design) {
        DataManager.SaveInt(this.getApplicationContext(), DataManager.KEY_SELECTEDBALL, design.id);
        _currentDesign = design;
        this.onReplayClicked();
    }

    class BallVH extends RecyclerView.ViewHolder {
        TextView tvEmoji;
        TextView tvScore;
        ShapeableImageView ivDesign;
        ShapeableImageView ivVolume;
        View clickableArea;

        public BallVH(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvScore = itemView.findViewById(R.id.tvScore);
            ivDesign = itemView.findViewById(R.id.ivDesign);
            ivVolume = itemView.findViewById(R.id.ivVolume);
            clickableArea = itemView;
        }
    }

    private class BallAdapter extends RecyclerView.Adapter<BallVH> {
        ArrayList<BallDesign> _designs;

        public BallAdapter() {
            _designs = DataManager.getAllBallsDesign(MainActivity.this.getBaseContext());
        }

        @NonNull
        @Override
        public BallVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BallVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_ball_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BallVH holder, int position) {

            final BallDesign currentDesign = _designs.get(position);

            int score = DataManager.getScore(MainActivity.this, currentDesign.id);
            boolean unlocked = DataManager.getUnlocked(MainActivity.this, currentDesign.id);

            holder.tvScore.setText("" + score);
            holder.tvScore.setBackgroundResource(DataManager.getBackgroundDrawable(score, getResources()));
            holder.tvScore.setVisibility(unlocked ? View.VISIBLE : View.GONE);
            holder.ivVolume.setVisibility(currentDesign.song > 0 ? View.VISIBLE : View.GONE);

            double luminance = ColorUtils.calculateLuminance(DataManager.getBackgroundColor(score, getResources()));
            holder.tvScore.setTextColor(luminance > 0.5 ? Color.BLACK : Color.WHITE);

            if (unlocked)
                holder.ivDesign.setStrokeColorResource(DataManager.getBackgroundResourceColor(score));
            else holder.ivDesign.setStrokeColorResource(R.color.gray);

            if (currentDesign.emoji != null) {
                holder.tvEmoji.setText(currentDesign.emoji);
                holder.tvEmoji.setVisibility(View.VISIBLE);
                holder.ivDesign.setImageDrawable(null);
            } else {
                Glide.with(MainActivity.this).load(currentDesign.drawable).override(PongView.convertDpToPixel(96), PongView.convertDpToPixel(96)).placeholder(R.drawable.baseline_accessibility_24).into(holder.ivDesign);

                if (unlocked) holder.ivDesign.clearColorFilter();
                else holder.ivDesign.setColorFilter(Color.argb(255, 0, 0, 0));
                holder.tvEmoji.setVisibility(View.GONE);
            }

            holder.clickableArea.setOnClickListener(v -> {
                MainActivity.this.onBallSelected(currentDesign);
            });

        }

        @Override
        public int getItemCount() {
            return _designs.size();
        }
    }
}