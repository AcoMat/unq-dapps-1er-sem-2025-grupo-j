package unq.dapp.grupoj.soccergenius.services.matches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.FootballDataApiException;
import unq.dapp.grupoj.soccergenius.exceptions.GenAiResponseException;
import unq.dapp.grupoj.soccergenius.model.Match;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchDto;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.MatchScrapingService;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;
import unq.dapp.grupoj.soccergenius.services.team.TeamService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import unq.dapp.grupoj.soccergenius.util.FootballDataIdsUtil;
import unq.dapp.grupoj.soccergenius.util.InputSanitizer;
import unq.dapp.grupoj.soccergenius.util.WhoScoredIdsUtil;

import java.util.List;

@Service
public class MatchService {
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    
    private final TeamService teamService;
    private final FootballDataApiService footballDataApiService;
    private final MatchScrapingService matchScrapingService;
    private final TeamScrapingService teamScrapingService;
    private final Client client;

    public MatchService(
            TeamService teamService,
            FootballDataApiService footballDataApiService,
            MatchScrapingService matchScrapingService,
            TeamScrapingService teamScrapingService,
            Client client
    ) {
        this.teamService = teamService;
        this.footballDataApiService = footballDataApiService;
        this.matchScrapingService = matchScrapingService;
        this.teamScrapingService = teamScrapingService;
        this.client = client;
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

    public String getMatchPredictionBetween(String team1Name, String team2Name) {
        team1Name = InputSanitizer.sanitizeInput(team1Name);
        team2Name = InputSanitizer.sanitizeInput(team2Name);
        logger.info("Solicitando predicción para partido entre equipos con ID {} y {}", team1Name, team2Name);
        /*
        Datos para predecir un partido
        - Ultimos 5 partidos de local de cada equipo
        - Ultimos 5 partidos de visitante de cada equipo
        - Ultimos partidos entre los dos equipos
        - Posicion en la tabla de la liga
        - Numero de rating del equipo
        */
        if (team1Name.isEmpty() || team2Name.isEmpty()) {
            throw new IllegalArgumentException("Los nombres de los equipos no pueden ser nulos");
        }
        // Obtener los Ids para Football Data API
        int footballDataTeam1Id = FootballDataIdsUtil.getTeamIdFromTeamName(team1Name);
        int footballDataTeam2Id = FootballDataIdsUtil.getTeamIdFromTeamName(team2Name);
        // Obtener los Ids para WhoScored
        int team1IdWhoScored = WhoScoredIdsUtil.getTeamIdFromTeamName(team1Name);
        int team2IdWhoScored = WhoScoredIdsUtil.getTeamIdFromTeamName(team2Name);

        TeamDto team1 = teamService.getTeamFromLaLigaById(team1IdWhoScored);
        TeamDto team2 = teamService.getTeamFromLaLigaById(team2IdWhoScored);

        List<FootballDataMatchDto> lastMatchesTeam1;
        List<FootballDataMatchDto> lastMatchesTeam2;
        List<FootballDataMatchDto> lastHomeMatchesTeam1 = null;
        List<FootballDataMatchDto> lastAwayMatchesTeam1 = null;
        List<FootballDataMatchDto> lastHomeMatchesTeam2 = null;
        List<FootballDataMatchDto> lastAwayMatchesTeam2 = null;

        try {
            lastMatchesTeam1 = footballDataApiService.getLastXMatchesFromTeam(footballDataTeam1Id, 25).getMatches();
            lastMatchesTeam2 = footballDataApiService.getLastXMatchesFromTeam(footballDataTeam2Id, 25).getMatches();
            lastHomeMatchesTeam1 = lastMatchesTeam1.stream()
                    .filter(match -> match.getHomeTeam().getId() != null && match.getHomeTeam().getId().equals(footballDataTeam1Id))
                    .limit(5)
                    .toList();

            lastAwayMatchesTeam1 = lastMatchesTeam1.stream()
                    .filter(match -> match.getAwayTeam().getId() != null && match.getAwayTeam().getId().equals(footballDataTeam1Id))
                    .limit(5)
                    .toList();

            lastHomeMatchesTeam2 = lastMatchesTeam2.stream()
                    .filter(match -> match.getHomeTeam().getId() != null && match.getHomeTeam().getId().equals(footballDataTeam2Id))
                    .limit(5)
                    .toList();

            lastAwayMatchesTeam2 = lastMatchesTeam2.stream()
                    .filter(match -> match.getAwayTeam().getId() != null && match.getAwayTeam().getId().equals(footballDataTeam2Id))
                    .limit(5)
                    .toList();
        } catch (FootballDataApiException e) {
            logger.error("Error al obtener los últimos partidos de los equipos {} y {}", team1Name, team2Name);
        }

        double rankTeam1 = teamScrapingService.getCurrentRankingOfTeam(team1IdWhoScored);
        double rankTeam2 = teamScrapingService.getCurrentRankingOfTeam(team2IdWhoScored);

        int team1Position = teamScrapingService.getCurrentPositionOnLeague(team1IdWhoScored);
        int team2Position = teamScrapingService.getCurrentPositionOnLeague(team2IdWhoScored);

        List<Match> lastMatchesBetweenTeams = matchScrapingService.getPreviousMeetings(team1.getName(), team2.getName());

        logger.debug("Convirtiendo datos de partidos a formato string para procesamiento");
        String homeMatchesTeam1String = convertMatchesToString(lastHomeMatchesTeam1);
        String awayMatchesTeam1String = convertMatchesToString(lastAwayMatchesTeam1);
        String homeMatchesTeam2String = convertMatchesToString(lastHomeMatchesTeam2);
        String awayMatchesTeam2String = convertMatchesToString(lastAwayMatchesTeam2);
        String previousMeetingsString = formatPreviousMeetings(lastMatchesBetweenTeams);

        logger.info("Preparando solicitud a Gemini AI para predicción de partido entre {} y {}", team1.getName(), team2.getName());

        try {
            logger.info("Enviando solicitud a Gemini AI");
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

            String result = response.text();
            logger.info("Predicción recibida de Gemini AI para partido {}-{}: {}", team1.getName(), team2.getName(), result);
            return result;
        } catch (Exception e) {
            throw new GenAiResponseException("Error al obtener la predicción de Gemini AI: " + e.getMessage());
        }
    }
}
