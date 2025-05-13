package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;
import unq.dapp.grupoj.soccergenius.services.historyLog.HistoryLogService;

import java.util.List;

@RestController
@RequestMapping("/history")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
})
@Tag(name = "History Log", description = "Operations related to history logs of requests, predictions, and comparisons.")
public class HistoryLogController {

    private final HistoryLogService historyLogService;

    public HistoryLogController(HistoryLogService historyLogService) {
        this.historyLogService = historyLogService;
    }

    @GetMapping
    @Operation(summary = "Stores and allows querying of performance requests, predictions and comparisons made.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of history logs")
    })
    public ResponseEntity<List<HistoryLog>> getHistoryLogs(){
        List<HistoryLog> historyLogList = historyLogService.getHistoryLogs();

        return ResponseEntity.status(200)
                             .body(historyLogList);
    }
}
