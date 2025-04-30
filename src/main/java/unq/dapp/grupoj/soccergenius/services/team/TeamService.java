package unq.dapp.grupoj.soccergenius.services.team;

import unq.dapp.grupoj.soccergenius.model.Player;

import java.util.List;

public interface TeamService {

    List<Player> getTeamPlayers(String teamName, String country);
}
