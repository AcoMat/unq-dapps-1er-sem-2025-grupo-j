package unq.dapp.grupoj.soccergenius.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import unq.dapp.grupoj.soccergenius.model.Team;

import java.util.List;

@ToString
@Setter
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompetitionDTO {
    private String name;
    private String id;
    private List<Team> teams;
}
