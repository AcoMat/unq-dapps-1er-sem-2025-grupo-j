package unq.dapp.grupoj.soccergenius.services.team;

import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.*;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.services.external.football_data.FootballDataApiService;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.TeamScrapingService;
import unq.dapp.grupoj.soccergenius.util.WhoScoredIdsUtil;
import unq.dapp.grupoj.soccergenius.util.InputSanitizer;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TeamScrapingService webScrapingService;

    private final FootballDataApiService footballDataApiService;
    private final TeamRepository teamRepository;
    private final Mapper mapper;

    public TeamServiceImpl
            (TeamScrapingService webScrapingService,
             TeamRepository teamRepository,
             Mapper mapper,
             FootballDataApiService footballDataApiService
        ) {
        this.webScrapingService = webScrapingService;
        this.footballDataApiService = footballDataApiService;
        this.teamRepository = teamRepository;
        this.mapper = mapper;
    }

    @Override
    public List<String> getTeamPlayers(String teamName, String country) {
        if (teamName == null || teamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        String requestedTeamName = InputSanitizer.sanitizeInput(teamName);
        String requestedCountry = InputSanitizer.sanitizeInput(country);

        logger.debug("Fetching players for team {} in country {}", requestedTeamName, requestedCountry);
        try {
            List<String> players = this.webScrapingService.getPlayersNamesFromTeam(requestedTeamName, requestedCountry);
            logger.debug("Retrieved {} players for team {}", players.size(), requestedTeamName);
            return players;
        } catch (NoSuchElementException e) {
            logger.error("Team not found: {}", requestedTeamName);
            throw new TeamNotFoundException("Team " + requestedTeamName + " not found in " + requestedCountry);
        }
    }

    @Override
    public List<MatchDTO> getUpcomingMatches(String teamName) {
        String requestedTeamName = InputSanitizer.sanitizeInput(teamName);

        logger.debug("Fetching upcoming matches for team {}", requestedTeamName);

        if (requestedTeamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }

        FootballDataMatchsDto footballDataMatchsDto = this.footballDataApiService.getUpcomingMatchesFromTeam(requestedTeamName);

        if (footballDataMatchsDto == null || footballDataMatchsDto.getMatches() == null) {
            logger.debug("No upcoming matches found for team {}", requestedTeamName);
            return List.of();
        }

        List<MatchDTO> upcomingMatches = footballDataMatchsDto.getMatches().stream()
                .map(match -> {
                    String homeTeamName = match.getHomeTeam() != null ? match.getHomeTeam().getName() : "Unknown";
                    String awayTeamName = match.getAwayTeam() != null ? match.getAwayTeam().getName() : "Unknown";
                    String utcDate = match.getUtcDate();

                    return new MatchDTO(homeTeamName, awayTeamName, "La Liga", utcDate);
                }).toList();

        logger.debug("Retrieved {} upcoming matches for team {}", upcomingMatches.size(), requestedTeamName);
        return upcomingMatches;

    }

    @Override
    public ComparisonDto getTeamsComparison(String teamAName, String teamBName) {
        String requestedTeamAName = InputSanitizer.sanitizeInput(teamAName);
        String requestedTeamBName = InputSanitizer.sanitizeInput(teamBName);

        if (requestedTeamAName.isEmpty() || requestedTeamBName.isEmpty()) {
            throw new IllegalArgumentException("Team names cannot be null or empty");
        }

        int teamAId = WhoScoredIdsUtil.getTeamIdFromTeamName(requestedTeamAName);
        int teamBId = WhoScoredIdsUtil.getTeamIdFromTeamName(requestedTeamBName);

        if (requestedTeamAName.equals(requestedTeamBName)) {
            throw new IllegalArgumentException("Cannot compare a team with itself");
        }

        logger.debug("Comparing teams {} and {}", requestedTeamAName, requestedTeamBName);

        TeamStatisticsDTO teamA = this.webScrapingService.scrapTeamStatisticsById(teamAId);
        TeamStatisticsDTO teamB = this.webScrapingService.scrapTeamStatisticsById(teamBId);

        return ComparisonDto.builder()
                                        .teamB(teamB)
                                        .teamA(teamA)
                                        .build();
    }

    @Override
    public TeamDto getTeamFromLaLigaById(int teamId) {
        logger.debug("Searching team in DB {} from La Liga", teamId);
        Team dbTeam = this.teamRepository.findById(teamId).orElse(null);
        if (dbTeam != null) {
            logger.debug("Found team in DB {} from La Liga", teamId);
            return this.mapper.toDTO(dbTeam);
        } else {
            logger.debug("Team not found in DB {} from La Liga", teamId);
            Team scrappedTeam = this.webScrapingService.scrapTeamDataById(teamId);
            teamRepository.save(scrappedTeam);
            logger.debug("Scraped team data for team {} from La Liga", teamId);
            return this.mapper.toDTO(scrappedTeam);
        }
    }
}