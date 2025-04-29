package unq.dapp.grupoj.soccergenius.services.player;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.dtos.PlayerPerformanceDto;
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

    public PlayerPerformanceDto getPlayerPerformance(int playerId) {


        throw new NotImplementedException();
    }
}
