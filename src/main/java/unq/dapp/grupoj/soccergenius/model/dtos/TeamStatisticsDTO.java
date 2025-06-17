package unq.dapp.grupoj.soccergenius.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class TeamStatisticsDTO {
    private String name;
    private String totalMatchesPlayedStr;
    private String totalGoalsStr;
    private String avgShotsPerGameStr;
    private String avgPossessionStr;
    private String avgPassSuccessStr;
    private String avgAerialWonPerGameStr;
    private String overallRatingStr;
    private String totalYellowCardsStr;
    private String totalRedCardsStr;
}
