import java.io.Serializable;

class GameInfo implements Serializable {
    private static final long serialVersionUID = 782039202L;
    boolean iWon;
    int myScore, oppScore;
    int iChose, oppChose;

    GameInfo(boolean iWon, int myScore, int oppScore, int iChose, int oppChose) {
        this.iWon = iWon;
        this.myScore = myScore;
        this.oppScore = oppScore;
        this.iChose = iChose;
        this.oppChose = oppChose;
    }
}

