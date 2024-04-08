package fr.mathis.pong;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PongView.PongListener {

    ConstraintLayout container;
    PongView _pongView;
    TextView _score;
    Button _replay;
    FloatingActionButton _setting;
    CardView _cvSettings;
    RecyclerView _ballRecyclerView;
    int currentBackgroundColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.main);
        _score = findViewById(R.id.score);
        _pongView = findViewById(R.id.pongGame);
        _replay = findViewById(R.id.btnReplay);
        _setting = findViewById(R.id.btnSettings);
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

        _setting.setOnClickListener(v -> {
            this.onSettingClicked();
        });

        _pongView.setListener(this);
        _pongView.start();

        _ballRecyclerView.setAdapter(new BallAdapter());
        _ballRecyclerView.setNestedScrollingEnabled(true);
    }

    @Override
    public void onScoreChanged(int score) {
        if (this._score != null) this._score.setText("" + score);

        this.updateBackgroundColor(score);
    }

    @Override
    public void onLost() {
        _replay.setVisibility(View.VISIBLE);
        _setting.setVisibility(View.VISIBLE);
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
        _setting.setVisibility(View.GONE);
        _cvSettings.setVisibility(View.GONE);
        _pongView.start();
    }

    private void onSettingClicked() {
        _cvSettings.setVisibility(View.VISIBLE);
        _setting.setVisibility(View.GONE);
    }

    class BallVH extends RecyclerView.ViewHolder {
        TextView tv;

        public BallVH(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.textView);
        }
    }

    private class BallAdapter extends RecyclerView.Adapter<BallVH> {
        ArrayList<BallDesign> _designs;

        public BallAdapter() {
            _designs = DataManager.getAllBallsDesign();
        }

        @NonNull
        @Override
        public BallVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BallVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_ball_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BallVH holder, int position) {
            holder.tv.setText("Icone " + position);
        }

        @Override
        public int getItemCount() {
            return _designs.size();
        }
    }
}