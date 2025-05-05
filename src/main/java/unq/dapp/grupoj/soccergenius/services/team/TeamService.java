package unq.dapp.grupoj.soccergenius.services.team;

import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.player.Player;

import java.util.List;

public interface TeamService {

    List<Player> getTeamPlayers(String teamName, String country);
    TeamDto getTeamFromLaLiga(String teamId);
    List<MatchDTO> getUpcomingMatches(String teamName);
}
