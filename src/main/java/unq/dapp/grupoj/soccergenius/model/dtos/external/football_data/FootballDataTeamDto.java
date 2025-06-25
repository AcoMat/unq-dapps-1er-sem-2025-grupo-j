package unq.dapp.grupoj.soccergenius.model.dtos.external.football_data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FootballDataTeamDto {
    private Integer id;
    private String name;
    private String shortName;
    private String tla;
    private String crest;
}
