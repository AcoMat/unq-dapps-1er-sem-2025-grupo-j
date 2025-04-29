package unq.dapp.grupoj.soccergenius.model.player.summary;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import unq.dapp.grupoj.soccergenius.model.player.Player;

@MappedSuperclass
public abstract class Summary {
    @Id
    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    //SUMMARY
    //private int totalGamesPlayed;
    //private int totalMinsPLayed;
    //private int totalGoals;
    //private int totalAssists;
    //private int totalYellowCards;
    //private int totalRedCards;

    private int rating;
}
