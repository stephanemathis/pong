package fr.mathis.pong;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Base64;

import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class DataManager {

    public static String KEY_UNLOCKED = "key_highscore_unlock";
    public static String KEY_HIGHSCORE = "key_highscore_max";
    public static String KEY_SELECTEDBALL = "key_selectedball";

    public static ArrayList<BallDesign> getAllBallsDesign(Context context) {

        ArrayList<BallDesign> designs = new ArrayList<BallDesign>();

        designs.add(new BallDesign(context.getResources(), 0, "\uD83D\uDE3B", 1f, 0)); // Chat coeur
        designs.add(new BallDesign(context.getResources(), 1, "❤\uFE0F", 1f, 0)); // Coeur rouge
        designs.add(new BallDesign(context.getResources(), 2, "\uD83E\uDD68", 1f, 0)); // Bretzel
        designs.add(new BallDesign(context.getResources(), 3, "✉\uFE0F", 1f, 0)); // Bretzel

        designs.add(new BallDesign(context.getResources(), 101, R.drawable.ball_crochet_bonbon, 2.5f, 0));
        designs.add(new BallDesign(context.getResources(), 102, R.drawable.ball_crochet_coeur, 1.5f, 0));
        designs.add(new BallDesign(context.getResources(), 103, R.drawable.ball_crochet_lapin, 1.5f, 0));
        designs.add(new BallDesign(context.getResources(), 104, R.drawable.ball_crochet_painepice, 1.6f, 0));

        designs.add(new BallDesign(context.getResources(), 105, R.drawable.ball_tate_gaelle_lapin, 2.5f, 0));
        designs.add(new BallDesign(context.getResources(), 109, R.drawable.ball_tete_gaelle_serviette, 2f, 0));
        designs.add(new BallDesign(context.getResources(), 110, R.drawable.ball_tete_gaelle_couronne, 1.7f, 0));
        designs.add(new BallDesign(context.getResources(), 111, R.drawable.ball_tete_stephane_deforme, 1.5f, 0));
        designs.add(new BallDesign(context.getResources(), 112, R.drawable.ball_tete_stephane_jeune, 1.5f, 0));
        designs.add(new BallDesign(context.getResources(), 113, R.drawable.ball_tete_stephane_serviette, 2f, 0));
        designs.add(new BallDesign(context.getResources(), 107, R.drawable.ball_tete_gautier, 1.4f, 0));
        designs.add(new BallDesign(context.getResources(), 108, R.drawable.ball_tete_therese, 1.6f, 0));

        designs.add(new BallDesign(context.getResources(), 106, R.drawable.ball_tete_ed, 1.5f, 0, R.raw.europapark));
        designs.add(new BallDesign(context.getResources(), 114, R.drawable.ball_tft_crue, 1.35f, 0));
        designs.add(new BallDesign(context.getResources(), 115, R.drawable.ball_tft_cuite, 1.35f, 0));
        designs.add(new BallDesign(context.getResources(), 116, R.drawable.ball_cigne_serviette, 2f, 0));
        designs.add(new BallDesign(context.getResources(), 120, R.drawable.ball_tasse, 1.8f, 0, R.raw.fastfurious));
        designs.add(new BallDesign(context.getResources(), 121, R.drawable.ball_dog, 2f, 0, R.raw.sax_guy));
        designs.add(new BallDesign(context.getResources(), 122, R.drawable.ball_cat_left, 2.5f, 0));
        designs.add(new BallDesign(context.getResources(), 123, R.drawable.ball_cat_right, 2.5f, 0));

        designs.add(new BallDesign(context.getResources(), 125, R.drawable.ball_trio_dals, 1.8f, 0));
        designs.add(new BallDesign(context.getResources(), 126, R.drawable.ball_trio_coiffure, 1.2f, 0));
        designs.add(new BallDesign(context.getResources(), 127, R.drawable.ball_trio_mariage_claire, 1.3f, 0));
        designs.add(new BallDesign(context.getResources(), 128, R.drawable.ball_trio_ober, 1.2f, 0));
        designs.add(new BallDesign(context.getResources(), 129, R.drawable.ball_trio_ep, 1.8f, 0));
        designs.add(new BallDesign(context.getResources(), 130, R.drawable.ball_trio_ober_recent, 1.2f, 0));

        designs.add(new BallDesign(context.getResources(), 117, R.drawable.ball_forme_gaellestephane, 5f, 0));
        designs.add(new BallDesign(context.getResources(), 118, R.drawable.ball_forme_gaelle_accrobranche, 5f, 0));
        designs.add(new BallDesign(context.getResources(), 119, R.drawable.ball_forme_therese, 3f, 0));

        return designs;
    }

    public static void saveScore(Context context, int ballId, int score) {
        SaveInt(context, KEY_HIGHSCORE + ballId, score);
    }

    public static int getScore(Context context, int ballId) {
        return ReadInt(context, KEY_HIGHSCORE + ballId, 0);
    }

    public static void saveUnlocked(Context context, int ballId, boolean unlocked) {
        SaveBool(context, KEY_UNLOCKED + ballId, unlocked);
    }

    public static boolean getUnlocked(Context context, int ballId) {
        return ReadBool(context, KEY_UNLOCKED + ballId, false);
    }

    public static BallDesign getCurrentBallDesign(Context context) {
        return getAllBallsDesign(context).stream().filter((b) -> b.id == DataManager.ReadInt(context, KEY_SELECTEDBALL, 0)).findFirst().orElse(getAllBallsDesign(context).get(0));
    }

    public static int getBackgroundColor(int score, Resources resources) {
        return ResourcesCompat.getColor(resources, DataManager.getBackgroundResourceColor(score), null);
    }

    public static int getBackgroundResourceColor(int score) {
        int newBackgroundColor;

        if (score < 5)
            newBackgroundColor = R.color.game_background;
        else if (score < 10)
            newBackgroundColor = R.color.game_background_05;
        else if (score < 15)
            newBackgroundColor = R.color.game_background_10;
        else if (score < 20)
            newBackgroundColor = R.color.game_background_15;
        else if (score < 25)
            newBackgroundColor = R.color.game_background_20;
        else if (score < 30)
            newBackgroundColor = R.color.game_background_25;
        else if (score < 35)
            newBackgroundColor = R.color.game_background_30;
        else if (score < 40)
            newBackgroundColor = R.color.game_background_35;
        else
            newBackgroundColor = R.color.game_background_40;

        return newBackgroundColor;
    }

    public static int getBackgroundDrawable(int score, Resources resources) {
        int newBackgroundColor;

        if (score < 5)
            newBackgroundColor = R.drawable.bg_round_score;
        else if (score < 10)
            newBackgroundColor = R.drawable.bg_round_score_05;
        else if (score < 15)
            newBackgroundColor = R.drawable.bg_round_score_10;
        else if (score < 20)
            newBackgroundColor = R.drawable.bg_round_score_15;
        else if (score < 25)
            newBackgroundColor = R.drawable.bg_round_score_20;
        else if (score < 30)
            newBackgroundColor = R.drawable.bg_round_score_25;
        else if (score < 35)
            newBackgroundColor = R.drawable.bg_round_score_30;
        else if (score < 40)
            newBackgroundColor = R.drawable.bg_round_score_35;
        else
            newBackgroundColor = R.drawable.bg_round_score_40;

        return newBackgroundColor;
    }

    public static int ReadInt(Context context, String key, int defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(mgr.getString(key, defaultValue + ""));
    }

    public static void SaveInt(Context context, String key, int value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putString(key, value + "");
        editor.commit();
    }

    public static boolean ReadBool(Context context, String key, boolean defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.getBoolean(key, defaultValue);
    }

    public static void SaveBool(Context context, String key, boolean value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String ReadString(Context context, String key, String defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.getString(key, defaultValue);
    }

    public static void SaveString(Context context, String key, String value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
