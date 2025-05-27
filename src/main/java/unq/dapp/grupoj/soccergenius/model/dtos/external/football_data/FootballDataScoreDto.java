package unq.dapp.grupoj.soccergenius.model.dtos.external.football_data;

import lombok.Getter;

@Getter
public class FootballDataScoreDto {
    private String winner;
    private String duration;
    private FootballDataScoreTimeDto fullTime;
    private FootballDataScoreTimeDto halfTime;
}
