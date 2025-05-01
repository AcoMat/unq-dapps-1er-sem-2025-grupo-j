package unq.dapp.grupoj.soccergenius.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@ToString
@Setter
@Getter
public class Team {
    private String id;
    private String name;
    private List<Player> playersList;
}
