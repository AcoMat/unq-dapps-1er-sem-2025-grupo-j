package unq.dapp.grupoj.soccergenius.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.player.Player;

import java.util.List;

@ToString
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Team {
    @Id
    private int id;
    private String name;
    private String country;
    private String league;

    @OneToMany(mappedBy = "actualTeam")
    @Setter
    private List<Player> playersList;

    public Team(int id, String name, String country, String league) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.league = league;
    }
}
