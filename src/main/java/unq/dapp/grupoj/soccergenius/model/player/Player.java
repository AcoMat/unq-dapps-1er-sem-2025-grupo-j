package unq.dapp.grupoj.soccergenius.model.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

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
    private CurrentParticipationsSummary currentParticipationsSummary;
    @OneToOne(cascade = CascadeType.ALL)
    private HistoricalParticipationsSummary history;

    private ZonedDateTime lastUpdate;

    public Player(int id, String name, int age, String nationality, String height, List<String> positions) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.nationality = nationality;
        this.height = height;
        this.positions = positions;
    }

    public String getPerformanceAndTendency() {
        double actualRating = this.currentParticipationsSummary.getRating();
        double historicalRatingAverage = this.history.getRating();

        return this.name + " has a current performance index of " + actualRating + " and a tendency of " + (actualRating - historicalRatingAverage) + " compared to the historical average.";
    }
}
