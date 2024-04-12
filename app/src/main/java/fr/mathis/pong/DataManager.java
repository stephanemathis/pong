package fr.mathis.pong;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class DataManager {

    public static String KEY_HIGHSCORE = "key_highscore";
    public static String KEY_SELECTEDBALL = "key_selectedball";

    public static ArrayList<BallDesign> getAllBallsDesign(Context context) {

        ArrayList<BallDesign> designs = new ArrayList<BallDesign>();

        designs.add(new BallDesign(context.getResources(), 0, -1, 1f, "\uD83D\uDE3B", 0)); // Chat coeur
        designs.add(new BallDesign(context.getResources(), 1, -1, 1f, "❤\uFE0F", 0)); // Coeur rouge
        designs.add(new BallDesign(context.getResources(), 2, -1, 1f, "\uD83E\uDD68", 0)); // Bretzel
        designs.add(new BallDesign(context.getResources(), 3, -1, 1f, "✉\uFE0F", 0)); // Bretzel



        designs.add(new BallDesign(context.getResources(), 101, R.drawable.ball_crochet_bonbon, 3f, null, 0));
        designs.add(new BallDesign(context.getResources(), 102, R.drawable.ball_crochet_coeur, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 103, R.drawable.ball_crochet_lapin, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 104, R.drawable.ball_crochet_painepice, 1.5f, null, 0));

        designs.add(new BallDesign(context.getResources(), 105, R.drawable.ball_tate_gaelle_lapin, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 109, R.drawable.ball_tete_gaelle_serviette, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 110, R.drawable.ball_tete_gaelle_couronne, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 111, R.drawable.ball_tete_stephane_deforme, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 112, R.drawable.ball_tete_stephane_jeune, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 113, R.drawable.ball_tete_stephane_serviette, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 107, R.drawable.ball_tete_gautier, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 108, R.drawable.ball_tete_therese, 1.5f, null, 0));

        designs.add(new BallDesign(context.getResources(), 106, R.drawable.ball_tete_ed, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 114, R.drawable.ball_tft_crue, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 115, R.drawable.ball_tft_cuite, 1.5f, null, 0));
        designs.add(new BallDesign(context.getResources(), 116, R.drawable.ball_cigne_serviette, 2.5f, null, 0));

        designs.add(new BallDesign(context.getResources(), 117, R.drawable.ball_forme_gaellestephane, 6f, null, 0));
        designs.add(new BallDesign(context.getResources(), 118, R.drawable.ball_forme_gaelle_accrobranche, 6f, null, 0));

        return designs;
    }

    public static BallDesign getCurrentBallDesign(Context context) {
        return getAllBallsDesign(context).stream()
                .filter((b) -> b.id == DataManager.ReadInt(context, KEY_SELECTEDBALL, 0))
                .findFirst()
                .orElse(getAllBallsDesign(context).get(0));
    }

    public static int getBackgroundColor(int score, Resources resources) {
        int newBackgroundColor;

        if (score < 5)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background, null);
        else if (score < 10)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_05, null);
        else if (score < 15)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_10, null);
        else if (score < 20)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_15, null);
        else if (score < 25)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_20, null);
        else if (score < 30)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_25, null);
        else if (score < 35)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_30, null);
        else if (score < 40)
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_35, null);
        else
            newBackgroundColor = ResourcesCompat.getColor(resources, R.color.game_background_40, null);

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

    private static Object fromString(String s) {
        try {
            byte b[] = Base64.decode(s, 0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = null;

            si = new ObjectInputStream(bi);


            return si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String toString(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return Base64.encodeToString(baos.toByteArray(), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
