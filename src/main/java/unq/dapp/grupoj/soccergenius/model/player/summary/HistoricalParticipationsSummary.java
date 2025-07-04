package unq.dapp.grupoj.soccergenius.model.player.summary;

import jakarta.persistence.*;
import unq.dapp.grupoj.soccergenius.model.player.Player;

@Entity
public class HistoricalParticipationsSummary extends Summary {

    public HistoricalParticipationsSummary(Player player, double rating) {
        super(player, rating);
    }

    public HistoricalParticipationsSummary() {
        super();
    }
}
