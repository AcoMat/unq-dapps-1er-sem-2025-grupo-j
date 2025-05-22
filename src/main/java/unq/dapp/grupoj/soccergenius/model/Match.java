package unq.dapp.grupoj.soccergenius.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Match {
    @Id
    private String id;
    private String date; // Added date
    private String homeTeam;
    private String awayTeam;
    private String winner; // Can be home team name, away team name, or "Draw"
    private String homeScore;
    private String awayScore;

}
