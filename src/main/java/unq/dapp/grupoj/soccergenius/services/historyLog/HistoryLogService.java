package unq.dapp.grupoj.soccergenius.services.historyLog;

import unq.dapp.grupoj.soccergenius.model.HistoryLog;

import java.util.List;

public interface HistoryLogService {

    void saveRequestLog(HistoryLog historyLog);

    List<HistoryLog> getHistoryLogs();
}
