package unq.dapp.grupoj.soccergenius.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Information about a soccer team")
public class TeamDto {
    @Schema(description = "Team name", example = "Manchester United")
    private String name;
    
    @Schema(description = "Country where the team is based", example = "England")
    private String country;
    
    @Schema(description = "League in which the team competes", example = "Premier League")
    private String league;
}
