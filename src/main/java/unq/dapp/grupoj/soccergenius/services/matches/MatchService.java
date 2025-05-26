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
        // Si la lista está vacía, devolver un mensaje indicativo
        if (matches == null || matches.isEmpty()) {
            return "No hay partidos registrados";
        }
        
        StringBuilder result = new StringBuilder();
        
        for (FootballDataMatchDto match : matches) {
            result.append(match.getHomeTeam().getName())
                  .append(" vs ")
                  .append(match.getAwayTeam().getName())
                  .append(" (")
                  .append(match.getUtcDate())
                  .append("): ");
                  
            if (match.getScore().getFullTime() != null) {
                result.append(match.getScore().getFullTime().getHome())
                      .append(" - ")
                      .append(match.getScore().getFullTime().getAway());
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

        // Obtener los IDs correctos para Football Data API
        Integer footballDataTeam1Id = footballDataApiService.convertWhoScoredIdToFootballDataId(team1Id);
        Integer footballDataTeam2Id = footballDataApiService.convertWhoScoredIdToFootballDataId(team2Id);
        
        System.out.println("ID original team1: " + team1Id + ", Football Data ID: " + footballDataTeam1Id);
        System.out.println("ID original team2: " + team2Id + ", Football Data ID: " + footballDataTeam2Id);
        
        List<FootballDataMatchDto> lastMatchesTeam1 = footballDataApiService.getLastXMatchesFromTeam(team1Id, 25).getMatches();
        List<FootballDataMatchDto> lastMatchesTeam2 = footballDataApiService.getLastXMatchesFromTeam(team2Id, 25).getMatches();

        System.out.println("Partidos recuperados para equipo 1: " + (lastMatchesTeam1 != null ? lastMatchesTeam1.size() : "null"));
        System.out.println("Partidos recuperados para equipo 2: " + (lastMatchesTeam2 != null ? lastMatchesTeam2.size() : "null"));

        assert lastMatchesTeam1 != null;
        List<FootballDataMatchDto> lastHomeMatchesTeam1 = lastMatchesTeam1.stream()
                .filter(match -> match.getHomeTeam().getId() != null && match.getHomeTeam().getId().equals(footballDataTeam1Id))
                .limit(5)
                .toList();

        List<FootballDataMatchDto> lastAwayMatchesTeam1 = lastMatchesTeam1.stream()
                .filter(match -> match.getAwayTeam().getId() != null && match.getAwayTeam().getId().equals(footballDataTeam1Id))
                .limit(5)
                .toList();

        assert lastMatchesTeam2 != null;
        List<FootballDataMatchDto> lastHomeMatchesTeam2 = lastMatchesTeam2.stream()
                .filter(match -> match.getHomeTeam().getId() != null && match.getHomeTeam().getId().equals(footballDataTeam2Id))
                .limit(5)
                .toList();

        List<FootballDataMatchDto> lastAwayMatchesTeam2 = lastMatchesTeam2.stream()
                .filter(match -> match.getAwayTeam().getId() != null && match.getAwayTeam().getId().equals(footballDataTeam2Id))
                .limit(5)
                .toList();

        System.out.println("Últimos partidos como local del equipo 1: " + lastHomeMatchesTeam1.size());
        System.out.println("Últimos partidos como visitante del equipo 1: " + lastAwayMatchesTeam1.size());
        System.out.println("Últimos partidos como local del equipo 2: " + lastHomeMatchesTeam2.size());
        System.out.println("Últimos partidos como visitante del equipo 2: " + lastAwayMatchesTeam2.size());

        double rankTeam1 = webScrapingService.getCurrentRankingOfTeam(team1Id);
        double rankTeam2 = webScrapingService.getCurrentRankingOfTeam(team2Id);

        int team1Position = webScrapingService.getCurrentPositionOnLeague(team1Id);
        int team2Position = webScrapingService.getCurrentPositionOnLeague(team2Id);

        List<Match> lastMatchesBetweenTeams = webScrapingService.getPreviousMeetings(team1.getName(), team2.getName());
        
        String homeMatchesTeam1String = convertMatchesToString(lastHomeMatchesTeam1);
        String awayMatchesTeam1String = convertMatchesToString(lastAwayMatchesTeam1);
        String homeMatchesTeam2String = convertMatchesToString(lastHomeMatchesTeam2);
        String awayMatchesTeam2String = convertMatchesToString(lastAwayMatchesTeam2);
        String previousMeetingsString = formatPreviousMeetings(lastMatchesBetweenTeams);
        
        System.out.println("String partidos locales equipo 1: " + homeMatchesTeam1String);
        
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash",
                        "Basándote en los siguientes datos, calcula SOLAMENTE las probabilidades para el partido " + 
                        team1.getName() + " vs " + team2.getName() + ".\n\n" +
                        "Datos:\n" +
                        "- Últimos partidos como local de " + team1.getName() + ": " + homeMatchesTeam1String + "\n" +
                        "- Últimos partidos como visitante de " + team1.getName() + ": " + awayMatchesTeam1String + "\n" +
                        "- Últimos partidos como local de " + team2.getName() + ": " + homeMatchesTeam2String + "\n" +
                        "- Últimos partidos como visitante de " + team2.getName() + ": " + awayMatchesTeam2String + "\n" +
                        "- " + team1.getName() + ": Posición " + team1Position + " en la liga, rating " + rankTeam1 + "\n" +
                        "- " + team2.getName() + ": Posición " + team2Position + " en la liga, rating " + rankTeam2 + "\n" +
                        "- Historial de enfrentamientos: " + previousMeetingsString + "\n\n" +
                        "RESPONDE USANDO EXACTAMENTE ESTE FORMATO (solo números):\n" +
                        "victoria_local%:empate%:victoria_visitante%\n\n" +
                        "Ejemplo: 45:25:30",
                        null);

        return response.text();
    }
}
