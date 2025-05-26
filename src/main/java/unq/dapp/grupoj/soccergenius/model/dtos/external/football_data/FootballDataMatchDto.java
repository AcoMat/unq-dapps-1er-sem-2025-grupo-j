package unq.dapp.grupoj.soccergenius.model.dtos.external.football_data;

import lombok.Getter;

@Getter
public class FootballDataMatchDto {
    private String utcDate;
    private FootballDataTeamDto homeTeam;
    private FootballDataTeamDto awayTeam;
    private FootballDataScoreDto score;
}
