package unq.dapp.grupoj.soccergenius.model.player.summary;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import unq.dapp.grupoj.soccergenius.model.player.Player;

@MappedSuperclass
@Getter
public abstract class Summary {
    @Id
    @JsonIgnore
    private int id;

    @OneToOne
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;

    //SUMMARY
    //private double totalGamesPlayed;
    //private double totalMinsPLayed;
    //private double totalGoals;
    //private double totalAssists;
    //private double totalYellowCards;
    //private double totalRedCards;

    private double rating;

    protected Summary() {
    }

    protected Summary(Player player, double rating) {
        this.id = player.getId();
        this.player = player;
        this.rating = rating;
    }
}
