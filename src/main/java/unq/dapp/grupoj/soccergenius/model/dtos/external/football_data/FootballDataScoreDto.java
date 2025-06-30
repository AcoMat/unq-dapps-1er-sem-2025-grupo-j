package unq.dapp.grupoj.soccergenius.model.dtos.external.football_data;

import lombok.Getter;

@Getter
public class FootballDataScoreDto {
    public String winner;
    public String duration;
    public FootballDataScoreTimeDto fullTime;
    public FootballDataScoreTimeDto halfTime;
}
