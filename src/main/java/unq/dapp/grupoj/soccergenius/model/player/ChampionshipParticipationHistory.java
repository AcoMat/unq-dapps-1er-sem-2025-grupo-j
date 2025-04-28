package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;

@Entity
public class ChampionshipParticipationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private ParticipationsHistorySumary playerHistory;

    private String championshipName;

    //private int totalGamesPlayed;
    //private int totalMinsPLayed;
    //private int totalGoals;
    //private int totalAssists;
    //private int totalYellowCards;
    //private int totalRedCards;

    private int rating;
}
