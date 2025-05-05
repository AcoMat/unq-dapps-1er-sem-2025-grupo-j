package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;
import unq.dapp.grupoj.soccergenius.services.historyLog.HistoryLogService;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryLogController {

    private final HistoryLogService historyLogService;

    public HistoryLogController(HistoryLogService historyLogService) {
        this.historyLogService = historyLogService;
    }

    @GetMapping
    @Operation(summary = "Almacena y permite consultar las solicitudes de rendimiento, predicciones y comparaciones realizadas.")
    public ResponseEntity<List<HistoryLog>> getHistoryLogs(){
        List<HistoryLog> historyLogList = historyLogService.getHistoryLogs();

        return ResponseEntity.status(200)
                             .body(historyLogList);
    }
}
