package unq.dapp.grupoj.soccergenius.services.history_log;

import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;
import unq.dapp.grupoj.soccergenius.repository.HistoryLogRespository;

import java.util.List;

@Service
public class HistoryLogServiceImpl implements HistoryLogService {
    public final HistoryLogRespository historyLogRespository;

    public HistoryLogServiceImpl(HistoryLogRespository historyLogRespository) {
        this.historyLogRespository = historyLogRespository;
    }

    @Override
    public void saveRequestLog(HistoryLog historyLog) {
        this.historyLogRespository.save(historyLog);
    }

    @Override
    public List<HistoryLog> getHistoryLogs() {
        return this.historyLogRespository.findAll();
    }
}
