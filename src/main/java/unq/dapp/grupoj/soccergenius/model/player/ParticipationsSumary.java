package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ParticipationsSumary {
    @Id
    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    //private int gamesPlayed;
    //private int minsPLayed;
    //private int goals;
    //private int assists;
    //private int yellowCards;
    //private int redCards;

    private int rating;

    //@OneToMany(mappedBy = "player")
    //private List<ChampionshipParticipation> participations;
}
