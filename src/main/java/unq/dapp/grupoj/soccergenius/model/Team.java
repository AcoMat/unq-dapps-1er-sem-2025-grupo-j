package unq.dapp.grupoj.soccergenius.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.player.Player;

import java.util.List;

@ToString
@Setter
@Getter
@Entity
public class Team {
    @Id
    private String id;
    private String name;

    @OneToMany(mappedBy = "actualTeam")
    private List<Player> playersList;
}
