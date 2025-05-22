package unq.dapp.grupoj.soccergenius.services.matches;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoScored.WebScrapingService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;

@Service
public class MatchService {

    private final TeamService teamService;
    private final FootballDataApiService footballDataApiService;
    private final WebScrapingService webScrapingService;

    public MatchService(TeamService teamService, FootballDataApiService footballDataApiService, WebScrapingService webScrapingService) {
        this.teamService = teamService;
        this.footballDataApiService = footballDataApiService;
        this.webScrapingService = webScrapingService;
    }

    public double getMatchPredictionBetween(int team1Id, int team2Id) {
        /*
        Datos para predecir un partido
        - Ultimos 5 partidos de local de cada equipo
        - Ultimos 5 partidos de visitante de cada equipo
        - Ultimos partidos entre los dos equipos
        - Posicion en la tabla de la liga
        - Numero de rating del equipo
        */
        TeamDto team1 = teamService.getTeamFromLaLigaById(team1Id);
        TeamDto team2 = teamService.getTeamFromLaLigaById(team2Id);

        return webScrapingService.getCurrentRankingOfTeam(team1Id);
    }
}
