package unq.dapp.grupoj.soccergenius.services.history_log;

import unq.dapp.grupoj.soccergenius.model.HistoryLog;

import java.util.List;

public interface HistoryLogService {

    void saveRequestLog(HistoryLog historyLog);

    List<HistoryLog> getHistoryLogs();
}
