package fr.mathis.pong;

import android.content.res.Resources;

import androidx.core.content.res.ResourcesCompat;

import java.sql.Array;
import java.util.ArrayList;

public class DataManager {


    public static ArrayList<BallDesign> getAllBallsDesign() {

        ArrayList<BallDesign> designs = new ArrayList<BallDesign>();

        designs.add(new BallDesign(0, R.drawable.baseline_settings_24, 0));
        designs.add(new BallDesign(1, R.drawable.ic_launcher_foreground, 0));
        designs.add(new BallDesign(2, R.drawable.ic_launcher_foreground, 0));
        designs.add(new BallDesign(3, R.drawable.ic_launcher_foreground, 0));
        designs.add(new BallDesign(4, R.drawable.ic_launcher_foreground, 0));
        designs.add(new BallDesign(5, R.drawable.ic_launcher_foreground, 0));

        return designs;


    }

    public static int getBackgroundColor(int score, Resources ressources) {
        int newBackgroundColor;

        if (score < 5)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background, null);
        else if (score < 10)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_05, null);
        else if (score < 15)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_10, null);
        else if (score < 20)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_15, null);
        else if (score < 25)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_20, null);
        else if (score < 30)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_25, null);
        else if (score < 35)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_30, null);
        else if (score < 40)
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_35, null);
        else
            newBackgroundColor = ResourcesCompat.getColor(ressources, R.color.game_background_40, null);

        return newBackgroundColor;
    }

}
