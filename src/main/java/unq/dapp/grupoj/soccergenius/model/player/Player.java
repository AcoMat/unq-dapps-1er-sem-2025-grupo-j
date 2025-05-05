package unq.dapp.grupoj.soccergenius.model.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

import java.time.ZonedDateTime;
import java.util.List;

@ToString
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
    @JsonManagedReference
    private Team actualTeam;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    @JsonManagedReference
    private CurrentParticipationsSummary currentParticipationsSummary;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    @JsonManagedReference
    private HistoricalParticipationsSummary history;

    private ZonedDateTime lastUpdate;

    public Player(int id, String name, int age, String nationality, String height, List<String> positions) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.nationality = nationality;
        this.height = height;
        this.positions = positions;
        this.lastUpdate = ZonedDateTime.now();
    }

    public void setAge(int age) {
        this.age = age;
        this.lastUpdate = ZonedDateTime.now();
    }

    public void setActualTeam(Team actualTeam) {
        this.actualTeam = actualTeam;
        this.lastUpdate = ZonedDateTime.now();
    }

    public void setCurrentParticipationsSummary(CurrentParticipationsSummary currentParticipationsSummary) {
        this.currentParticipationsSummary = currentParticipationsSummary;
        this.lastUpdate = ZonedDateTime.now();
    }

    public void setHistory(HistoricalParticipationsSummary history) {
        this.history = history;
        this.lastUpdate = ZonedDateTime.now();
    }

    @JsonIgnore
    public String getPerformanceAndTendency() {
        double actualRating = this.currentParticipationsSummary.getRating();
        double historicalRatingAverage = this.history.getRating();

        return this.name + " has a current performance index of " + actualRating + " and a tendency of " + (actualRating - historicalRatingAverage) + " compared to the historical average.";
    }
}
