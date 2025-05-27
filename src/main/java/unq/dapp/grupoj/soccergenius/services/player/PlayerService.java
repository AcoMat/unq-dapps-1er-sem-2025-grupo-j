package unq.dapp.grupoj.soccergenius.services.player;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.services.external.whoscored.PlayerScrapingService;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerScrapingService webScrapingService;

    public PlayerService(PlayerRepository playerRepository,PlayerScrapingService webScrapingService) {
        this.playerRepository = playerRepository;
        this.webScrapingService = webScrapingService;
    }

    public Player getPlayer(int playerId) {
        //TODO: check last update and refesh data
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            player = webScrapingService.scrapPlayerData(playerId);
            if (player == null) {
                return null;
            }
            player.setCurrentParticipationsSummary(webScrapingService.getCurrentParticipationInfo(player));
            player.setHistory(webScrapingService.getHistoryInfo(player));
            playerRepository.save(player);
        }
        return player;
    }

    public String getPlayerPerformance(int playerId) {
        Player player = this.getPlayer(playerId);
        if(player == null) {
            return "Player not found";
        }
        return player.getPerformanceAndTendency();
    }
}
