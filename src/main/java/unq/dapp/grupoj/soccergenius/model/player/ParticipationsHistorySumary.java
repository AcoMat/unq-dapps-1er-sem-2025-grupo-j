package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ParticipationsHistorySumary {
    @Id
    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    //private int totalGamesPlayed;
    //private int totalMinsPLayed;
    //private int totalGoals;
    //private int totalAssists;
    //private int totalYellowCards;
    //private int totalRedCards;

    @OneToMany(mappedBy = "playerHistory")
    private List<ChampionshipParticipationHistory> history;

    private int rating;
}
