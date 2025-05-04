package unq.dapp.grupoj.soccergenius.services.player;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;
import unq.dapp.grupoj.soccergenius.services.scrapping.WebScrapingService;

@Service
public class PlayerService {

    private final PlayerRepository _playerRepository;
    private final WebScrapingService _webScrapingService;

    public PlayerService(PlayerRepository playerRepository) {
        this._playerRepository = playerRepository;
        this._webScrapingService = new WebScrapingService();
    }

    public Player getPlayer(int playerId) {
        return _playerRepository.findById(playerId)
                .orElseGet(() -> _webScrapingService.scrapPlayerData(playerId));
    }

    public String getPlayerPerformance(int playerId) {
        Player player = _playerRepository.findById(playerId)
                .orElseGet(() -> _webScrapingService.scrapPlayerData(playerId));
        return player.getPerformanceAndTendency();
    }
}
