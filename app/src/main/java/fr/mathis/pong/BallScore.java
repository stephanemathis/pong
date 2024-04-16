package fr.mathis.pong;

import java.io.Serializable;

public class BallScore implements Serializable {

    public int id;
    public boolean unlocked;
    public int maxScore;

    public BallScore(int id, boolean unlocked, int maxScore) {


        this.id = id;
        this.unlocked = unlocked;
        this.maxScore = maxScore;
    }
}
