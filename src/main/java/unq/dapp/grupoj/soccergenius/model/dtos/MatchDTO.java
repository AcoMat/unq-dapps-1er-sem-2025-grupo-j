package unq.dapp.grupoj.soccergenius.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Information about a soccer match")
public class MatchDTO {
    @Schema(description = "Home team name", example = "Barcelona")
    private String localTeam;
    
    @Schema(description = "Away team name", example = "Real Madrid")
    private String visitorTeam;
    
    @Schema(description = "Competition name", example = "La Liga")
    private String competition;
    
    @Schema(description = "Match date and time in UTC", example = "2023-04-15T15:00:00Z")
    private String utcDate;


    @JsonProperty("homeTeam")
    private void unpackLocalTeam(Map<String,Object> homeTeam) {
        this.localTeam = (String)homeTeam.get("name");
    }

    @JsonProperty("awayTeam")
    private void unpackVisitorTeam(Map<String,Object> awayTeam) {
        this.visitorTeam = (String)awayTeam.get("name");
    }

    @JsonProperty("competition")
    private void unpackCompetition(Map<String,Object> competition) {
        this.competition = (String)competition.get("name");
    }

}
