package unq.dapp.grupoj.soccergenius.model.dtos.external.football_data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class FootballDataMatchsDto {
    private List<FootballDataMatchDto> matches;
}
