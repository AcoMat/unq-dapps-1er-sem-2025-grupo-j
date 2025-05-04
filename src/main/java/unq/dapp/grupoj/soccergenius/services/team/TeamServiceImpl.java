package unq.dapp.grupoj.soccergenius.services.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.mappers.Mapper;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.dtos.TeamDto;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.TeamRepository;
import unq.dapp.grupoj.soccergenius.services.scrapping.WebScrapingService;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final WebScrapingService webScrapingService;
    private final TeamRepository teamRepository;
    private final Mapper mapper;

    public TeamServiceImpl(WebScrapingService webScrapingService, TeamRepository teamRepository, Mapper mapper) {
        this.webScrapingService = webScrapingService;
        this.teamRepository = teamRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Player> getTeamPlayers(String teamName, String country) {
        logger.debug("Fetching players for team {} in country {}", teamName, country);
        try {
            List<Player> players = this.webScrapingService.getPlayersFromTeam(teamName, country);
            logger.debug("Retrieved {} players for team {}", players.size(), teamName);
            return players;
        } catch (Exception e) {
            logger.error("Error in TeamService while fetching players: {}", e.getMessage(), e);
            throw e;
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