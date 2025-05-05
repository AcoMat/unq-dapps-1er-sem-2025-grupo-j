package unq.dapp.grupoj.soccergenius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;

public interface HistoryLogRespository extends JpaRepository<HistoryLog, Long> {
}
