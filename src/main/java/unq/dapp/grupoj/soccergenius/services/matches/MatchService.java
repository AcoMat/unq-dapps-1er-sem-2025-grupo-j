package unq.dapp.grupoj.soccergenius.services.matches;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.Match;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoScored.WebScrapingService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.List;

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

    /**
     * Convierte una lista de partidos a un formato de string legible para Gemini AI
     */
    private String convertMatchesToString(List<FootballDataMatchDto> matches) {
        StringBuilder result = new StringBuilder();
        
        for (FootballDataMatchDto match : matches) {
            result.append(match.homeTeam.name)
                  .append(" vs ")
                  .append(match.awayTeam.name)
                  .append(" (")
                  .append(match.utcDate)
                  .append("): ");
                  
            if (match.score.fullTime != null) {
                result.append(match.score.fullTime.home)
                      .append(" - ")
                      .append(match.score.fullTime.away);
            } else {
                result.append("Pendiente");
            }
            
            result.append("; ");
        }
        
        return result.toString();
    }
    
    /**
     * Convierte los encuentros previos entre equipos a un formato legible
     */
    private String formatPreviousMeetings(List<Match> matches) {
        StringBuilder result = new StringBuilder();
        
        if (matches.isEmpty()) {
            return "No hay encuentros previos registrados";
        }
        
        for (Match match : matches) {
            result.append(match.getHomeTeam())
                  .append(" vs ")
                  .append(match.getAwayTeam())
                  .append(" (")
                  .append(match.getDate())
                  .append("): ")
                  .append(match.getHomeScore())
                  .append(" - ")
                  .append(match.getAwayScore())
                  .append("; ");
        }
        
        return result.toString();
    }

    public String getMatchPredictionBetween(int team1Id, int team2Id) {
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

        List<FootballDataMatchDto> lastHomeMatchesTeam1 = footballDataApiService.getLastXMatchesFromTeam(team1Id, 5).matches.stream().filter(
            match -> match.homeTeam.id.equals(footballDataApiService.convertWhoScoredIdToFootballDataId(team1Id))
        ).toList();
        List<FootballDataMatchDto> lastAwayMatchesTeam1 = footballDataApiService.getLastXMatchesFromTeam(team1Id, 5).matches.stream().filter(
            match -> match.awayTeam.id.equals(footballDataApiService.convertWhoScoredIdToFootballDataId(team1Id))
        ).toList();
        List<FootballDataMatchDto> lastHomeMatchesTeam2 = footballDataApiService.getLastXMatchesFromTeam(team2Id, 5).matches.stream().filter(
            match -> match.homeTeam.id.equals(footballDataApiService.convertWhoScoredIdToFootballDataId(team2Id))
        ).toList();
        List<FootballDataMatchDto> lastAwayMatchesTeam2 = footballDataApiService.getLastXMatchesFromTeam(team2Id, 5).matches.stream().filter(
            match -> match.awayTeam.id.equals(footballDataApiService.convertWhoScoredIdToFootballDataId(team2Id))
        ).toList();

        double rankTeam1 = webScrapingService.getCurrentRankingOfTeam(team1Id);
        double rankTeam2 = webScrapingService.getCurrentRankingOfTeam(team2Id);

        int team1Position = webScrapingService.getCurrentPositionOnLeague(team1Id);
        int team2Position = webScrapingService.getCurrentPositionOnLeague(team2Id);

        List<Match> lastMatchesBetweenTeams = webScrapingService.getPreviousMeetings(team1.getName(), team2.getName());
        
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash",
                        "Basándote en los siguientes datos, calcula SOLAMENTE las probabilidades para el partido " + 
                        team1.getName() + " vs " + team2.getName() + ".\n\n" +
                        "Datos:\n" +
                        "- Últimos partidos como local de " + team1.getName() + ": " + convertMatchesToString(lastHomeMatchesTeam1) + "\n" +
                        "- Últimos partidos como visitante de " + team1.getName() + ": " + convertMatchesToString(lastAwayMatchesTeam1) + "\n" +
                        "- Últimos partidos como local de " + team2.getName() + ": " + convertMatchesToString(lastHomeMatchesTeam2) + "\n" +
                        "- Últimos partidos como visitante de " + team2.getName() + ": " + convertMatchesToString(lastAwayMatchesTeam2) + "\n" +
                        "- " + team1.getName() + ": Posición " + team1Position + " en la liga, rating " + rankTeam1 + "\n" +
                        "- " + team2.getName() + ": Posición " + team2Position + " en la liga, rating " + rankTeam2 + "\n" +
                        "- Historial de enfrentamientos: " + formatPreviousMeetings(lastMatchesBetweenTeams) + "\n\n" +
                        "RESPONDE USANDO EXACTAMENTE ESTE FORMATO (solo números):\n" +
                        "victoria_local%:empate%:victoria_visitante%\n\n" +
                        "Ejemplo: 45:25:30",
                        null);

        return response.text();
    }
}
