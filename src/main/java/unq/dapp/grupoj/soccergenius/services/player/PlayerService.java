package unq.dapp.grupoj.soccergenius.services.player;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.services.external.whoScored.WebScrapingService;

@Service
public class PlayerService {

    private final PlayerRepository _playerRepository;
    private final WebScrapingService _webScrapingService;

    public PlayerService(PlayerRepository playerRepository) {
        this._playerRepository = playerRepository;
        this._webScrapingService = new WebScrapingService();
    }

    public Player getPlayer(int playerId) {
        //TODO: check last update and refesh data
        Player player = _playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            player = _webScrapingService.scrapPlayerData(playerId);
            if (player == null) {
                return null;
            }
            player.setCurrentParticipationsSummary(_webScrapingService.getCurrentParticipationInfo(player));
            player.setHistory(_webScrapingService.getHistoryInfo(player));
            _playerRepository.save(player);
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
