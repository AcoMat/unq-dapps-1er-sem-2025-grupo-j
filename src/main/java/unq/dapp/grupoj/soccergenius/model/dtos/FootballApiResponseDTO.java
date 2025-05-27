package unq.dapp.grupoj.soccergenius.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FootballApiResponseDTO {
    private List<MatchDTO> matches;
}
