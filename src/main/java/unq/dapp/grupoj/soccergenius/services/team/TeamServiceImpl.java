package unq.dapp.grupoj.soccergenius.services.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.Player;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.CompetitionDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.FootballApiResponseDTO;
import unq.dapp.grupoj.soccergenius.model.dtos.MatchDTO;
import unq.dapp.grupoj.soccergenius.services.scrapping.WebScrapingService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final WebScrapingService webScrapingService;
    private final RestTemplate restTemplate;
    private final TeamRepository teamRepository;
    private final Mapper mapper;

    public TeamServiceImpl(WebScrapingService webScrapingService, RestTemplate restTemplate) {
    public TeamServiceImpl(WebScrapingService webScrapingService, TeamRepository teamRepository, Mapper mapper) {
        this.webScrapingService = webScrapingService;
        this.restTemplate = restTemplate;
        this.teamRepository = teamRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Player> getTeamPlayers(String teamName, String country) {

        //TODO: abstract sanitization to a method
        String requestedTeamName = teamName.replaceAll("[\n\r]", "_");
        String requestedCountry = country.replaceAll("[\n\r]", "_");

        logger.debug("Fetching players for team {} in country {}", requestedTeamName, requestedCountry);
        try {
            List<Player> players = this.webScrapingService.scrapeWebsite(requestedTeamName, requestedCountry);
            logger.debug("Retrieved {} players for team {}", players.size(), requestedTeamName);
            return players;
        } catch (Exception e) {
            throw new ScrappingException(e.getMessage());
        }
    }

    @Override
    public List<MatchDTO> getUpcomingMatches(String teamName) {
        final String FOOTBALLDATA_SECRET = System.getenv("FOOTBALLDATA_TOKEN");
        String apiUrlGetTeams = "https://api.football-data.org/v4/competitions/2014/teams";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token",FOOTBALLDATA_SECRET);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CompetitionDTO> competitionDTOResponseEntity = restTemplate.exchange(apiUrlGetTeams, HttpMethod.GET,entity, CompetitionDTO.class);

        List<Team> competitionTeams = competitionDTOResponseEntity.getBody().getTeams();

        String idTeam = competitionTeams.stream()
                .filter(team -> team.getName().toLowerCase().contains(teamName.toLowerCase()))
                .findFirst()
                .map(Team::getId)
                .orElseThrow(()->new RuntimeException("Equipo no encontrado: " + teamName));

        String apiUrlGetOneTeam = "https://api.football-data.org/v4/teams/id/matches?status=SCHEDULED".replace("id",idTeam);

        ResponseEntity<FootballApiResponseDTO> response = restTemplate.exchange(apiUrlGetOneTeam,HttpMethod.GET,entity,FootballApiResponseDTO.class);

        FootballApiResponseDTO apiResponse = response.getBody();

        try {
            if (apiResponse != null && apiResponse.getMatches() != null) {
                List<MatchDTO> upcomingMatches = apiResponse.getMatches().stream()
                        .map(matchDto -> {
                            String homeTeamName = matchDto.getLocalTeam() != null ? matchDto.getLocalTeam() : "Unknown";
                            String awayTeamName = matchDto.getVisitorTeam() != null ? matchDto.getVisitorTeam() : "Unknown";
                            String competition = matchDto.getCompetition() != null ? matchDto.getCompetition() : "Unknown";

                            return new MatchDTO(homeTeamName, awayTeamName, competition, matchDto.getUtcDate());
                        })
                        .collect(Collectors.toList());

                logger.debug("Retrieved {} upcoming matches for team {}", upcomingMatches.size(), teamName);
                return upcomingMatches;
            } else {
                logger.warn("No upcoming matches found or error in API response for team {}", teamName);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error fetching upcoming matches for team {}: {}", teamName, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public TeamDto getTeamFromLaLiga(int teamId) {
        logger.debug("Searching team in DB {} from La Liga", teamId);
        Team dbTeam = this.teamRepository.findById(teamId).orElse(null);
        if (dbTeam != null) {
            logger.debug("Found team in DB {} from La Liga", teamId);
            return this.mapper.toDTO(dbTeam);
        } else {
            logger.debug("Team not found in DB {} from La Liga", teamId);
            Team scrappedTeam = this.webScrapingService.scrapTeamData(teamId);
            teamRepository.save(scrappedTeam);
            logger.debug("Scraped team data for team {} from La Liga", teamId);
            return this.mapper.toDTO(scrappedTeam);
        }
    }
}