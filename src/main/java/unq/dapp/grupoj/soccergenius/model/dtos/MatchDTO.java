package unq.dapp.grupoj.soccergenius.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDTO {
    private String localTeam;
    private String visitorTeam;
    private String competition;
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
