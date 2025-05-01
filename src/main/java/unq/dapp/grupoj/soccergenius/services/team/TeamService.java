package unq.dapp.grupoj.soccergenius.services.team;

import unq.dapp.grupoj.soccergenius.model.Player;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;

import java.util.List;

public interface TeamService {

    List<Player> getTeamPlayers(String teamName, String country);
    List<MatchDTO> getUpcomingMatches(String teamName);
}
