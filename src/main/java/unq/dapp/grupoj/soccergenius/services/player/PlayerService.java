package unq.dapp.grupoj.soccergenius.services.player;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.dtos.PlayerPerformanceDto;
import unq.dapp.grupoj.soccergenius.repository.PlayerRepository;

@Service
public class PlayerService {

    private final PlayerRepository _playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this._playerRepository = playerRepository;
    }

    public PlayerPerformanceDto getPlayerPerformance(Long playerId) {
        //TODO

        throw new NotImplementedException();
    }
}
