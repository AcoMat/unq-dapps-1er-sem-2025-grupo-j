package unq.dapp.grupoj.soccergenius.model.player.summary;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import unq.dapp.grupoj.soccergenius.model.player.Player;

@MappedSuperclass
@Getter
public abstract class Summary {
    @Id
    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    //SUMMARY
    //private double totalGamesPlayed;
    //private double totalMinsPLayed;
    //private double totalGoals;
    //private double totalAssists;
    //private double totalYellowCards;
    //private double totalRedCards;

    private double rating;

    public Summary() {
    }

    public Summary(Player player, double rating) {
        this.player = player;
        this.rating = rating;
    }
}
