package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.Team;

import java.time.ZonedDateTime;
import java.util.List;

@ToString
@Setter
@Getter
@Entity
@NoArgsConstructor
public class Player {
    @Id
    private int id;

    private String name;
    private int age;
    private String nationality;
    private String height;
    private List<String> positions;

    @ManyToOne
    @JoinColumn(name = "actual_team_id")
    private Team actualTeam;
    @OneToOne(cascade = CascadeType.ALL)
    private ParticipationsSumary actualParticipations;
    @OneToOne(cascade = CascadeType.ALL)
    private ParticipationsHistorySumary history;

    private ZonedDateTime lastUpdate;
}
