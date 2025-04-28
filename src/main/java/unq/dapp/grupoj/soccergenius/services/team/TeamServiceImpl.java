package unq.dapp.grupoj.soccergenius.services.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.Player;
import unq.dapp.grupoj.soccergenius.services.scrapping.WebScrapingService;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final WebScrapingService webScrapingService;

    public TeamServiceImpl(WebScrapingService webScrapingService) {
        this.webScrapingService = webScrapingService;
    }

    @Override
    public List<Player> getTeamPlayers(String teamName, String country) {
        logger.debug("Fetching players for team {} in country {}", teamName, country);
        try {
            List<Player> players = this.webScrapingService.scrapeWebsite(teamName, country);
            logger.debug("Retrieved {} players for team {}", players.size(), teamName);
            return players;
        } catch (Exception e) {
            logger.error("Error in TeamService while fetching players: {}", e.getMessage(), e);
            throw e;
        }
    }
}