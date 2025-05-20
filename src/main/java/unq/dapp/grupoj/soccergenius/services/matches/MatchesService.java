package unq.dapp.grupoj.soccergenius.services.matches;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

@Service
public class MatchesService {

    private final TeamService teamService;

    public MatchesService(TeamService teamService) {
        this.teamService = teamService;
    }

    public String getMatchPredictionBetween(String team1Id, String team2Id) {
        /*
        Datos para predecir un partido
        - Ultimos 5 partidos de local de cada equipo (filter getLastXMatchesFromTeam)
        - Ultimos 5 partidos de visitante de cada equipo (filter getLastXMatchesFromTeam)
        - Ultimos 10 partidos entre los dos equipos
        - Posicion en la tabla de la liga
        - Numero de rating del equipo
        */
        throw new NotImplementedException();
    }
}
