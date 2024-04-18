package fr.mathis.pong;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity implements PongView.PongListener {

    ConstraintLayout container;
    PongView _pongView;
    TextView _score;
    Button _replay;
    ImageView _ivSettings;
    CardView _cvSettings;
    RecyclerView _ballRecyclerView;
    BallAdapter _adapter;
    int currentBackgroundColor;
    BallDesign currentDesign;
    MediaPlayer mediaPlayer;


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

        _replay.setOnClickListener(v -> {
            this.onReplayClicked();
        });

        _ivSettings.setOnClickListener(v -> {
            this.onSettingClicked();
        });

        _pongView.setListener(this);

        _adapter = new BallAdapter();
        _ballRecyclerView.setAdapter(_adapter);
        _ballRecyclerView.setNestedScrollingEnabled(true);

        currentDesign = DataManager.getCurrentBallDesign(this);
    }

    boolean paused = false;

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        this.paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.paused) {
            this.paused = false;
            this.onReplayClicked();
            if (mediaPlayer != null) {
                mediaPlayer.start();
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

        DataManager.saveScore(getBaseContext(), ballId, finalScore);
        DataManager.saveUnlocked(getBaseContext(), ballId, true);

        int i = 0;

        for (; i < _adapter._designs.size(); i++)
            if (_adapter._designs.get(i).id == ballId) break;

        _ballRecyclerView.getAdapter().notifyItemChanged(i);
    }

    private void updateBackgroundColor(int score) {
        int newBackgroundColor = DataManager.getBackgroundColor(score, getResources());

        if (currentBackgroundColor != newBackgroundColor) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentBackgroundColor, newBackgroundColor);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(animator -> container.setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimation.start();

            currentBackgroundColor = newBackgroundColor;
        }
    }

    private void onReplayClicked() {
        _replay.setVisibility(View.GONE);
        _ivSettings.setVisibility(View.GONE);
        _cvSettings.setVisibility(View.GONE);
        _pongView.start();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (currentDesign.song > 0) {
            try {
                mediaPlayer = MediaPlayer.create(this, currentDesign.song);


                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();


            } catch (IllegalStateException e) {
                mediaPlayer = null;
            }
        }

    }

    private void onSettingClicked() {
        _cvSettings.setVisibility(_cvSettings.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }


    public void onBallSelected(BallDesign design) {
        DataManager.SaveInt(this.getApplicationContext(), DataManager.KEY_SELECTEDBALL, design.id);
        currentDesign = design;
        this.onReplayClicked();
    }

    class BallVH extends RecyclerView.ViewHolder {
        TextView tvEmoji;
        TextView tvScore;
        ImageView ivDesign;
        View clickableArea;

        public BallVH(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvScore = itemView.findViewById(R.id.tvScore);
            ivDesign = itemView.findViewById(R.id.ivDesign);
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

            double luminance = ColorUtils.calculateLuminance(DataManager.getBackgroundColor(score, getResources()));
            holder.tvScore.setTextColor(luminance > 0.5 ? Color.BLACK : Color.WHITE);

            if (currentDesign.emoji != null) {
                holder.tvEmoji.setText(currentDesign.emoji);
                holder.tvEmoji.setVisibility(View.VISIBLE);
                holder.ivDesign.setVisibility(View.GONE);
            } else {
                Glide.with(MainActivity.this).load(currentDesign.drawable).override(PongView.convertDpToPixel(96), PongView.convertDpToPixel(96)).placeholder(R.drawable.baseline_accessibility_24).into(holder.ivDesign);

                if (unlocked) holder.ivDesign.clearColorFilter();
                else holder.ivDesign.setColorFilter(Color.argb(255, 0, 0, 0));
                holder.tvEmoji.setVisibility(View.GONE);
                holder.ivDesign.setVisibility(View.VISIBLE);
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