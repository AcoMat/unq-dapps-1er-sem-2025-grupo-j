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

//    // Getters y Setters
//    public List<MatchDTO> getMatches() {
//        return matches;
//    }
//
//    public void setMatches(List<MatchDTO> matches) {
//        this.matches = matches;
//    }
}
