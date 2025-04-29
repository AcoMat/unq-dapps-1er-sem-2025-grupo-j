package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

public class HistoricalParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private HistoricalParticipationsSummary playerHistory;

    private String season;
    private String championshipName;

    //private int totalGamesPlayed;
    //private int totalMinsPLayed;
    //private int totalGoals;
    //private int totalAssists;
    //private int totalYellowCards;
    //private int totalRedCards;

    private int rating;
}
