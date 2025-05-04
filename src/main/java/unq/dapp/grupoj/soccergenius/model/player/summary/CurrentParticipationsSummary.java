package unq.dapp.grupoj.soccergenius.model.player.summary;

import jakarta.persistence.*;
import unq.dapp.grupoj.soccergenius.model.player.Player;

@Entity
public class CurrentParticipationsSummary extends Summary {

    //@OneToMany(mappedBy = "player")
    //private List<ChampionshipParticipation> participations;

    public CurrentParticipationsSummary() {
        super();
    }

    public CurrentParticipationsSummary(Player player, double rating) {
        super(player, rating);
    }
}
