package unq.dapp.grupoj.soccergenius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.dapp.grupoj.soccergenius.model.Team;

public interface TeamRepository extends JpaRepository<Team, Integer> {
}
