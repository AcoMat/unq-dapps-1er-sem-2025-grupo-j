package unq.dapp.grupoj.soccergenius.services.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
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
}