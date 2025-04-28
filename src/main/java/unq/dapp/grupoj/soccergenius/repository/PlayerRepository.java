package unq.dapp.grupoj.soccergenius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.dapp.grupoj.soccergenius.model.player.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
