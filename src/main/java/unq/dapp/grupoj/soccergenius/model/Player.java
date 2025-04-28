package unq.dapp.grupoj.soccergenius.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class Player {

    //TODO: ADD HISTORICAL STATS, CONTEMPLATE SUMMARY STATS, ADD POSITION, ACTUAL TEAM, ETC.
    private String name;
    private String gamesPlayed;
    private String goals;
    private String assists;
    private String rating;

    public Player(String name, String gamesPlayed, String goals, String assists, String rating) {
        this.name        = name;
        this.gamesPlayed = gamesPlayed;
        this.goals       = goals;
        this.assists     = assists;
        this.rating      = rating != null ? rating : "0.0";
    }


}
