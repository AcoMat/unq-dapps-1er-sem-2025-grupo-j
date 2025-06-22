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

    private double rating;

    protected Summary() {
    }

    protected Summary(Player player, double rating) {
        this.id = player.getId();
        this.player = player;
        this.rating = rating;
    }
}
