package unq.dapp.grupoj.soccergenius.services.team;

import unq.dapp.grupoj.soccergenius.model.dtos.ComparisonDto;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;

import java.util.List;

public interface TeamService {

    List<String> getTeamPlayers(String teamName, String country);
    TeamDto getTeamFromLaLigaById(int teamId);
    List<MatchDTO> getUpcomingMatches(String teamName);
    ComparisonDto getTeamsComparison(String teamIdA, String teamIdB);
}
