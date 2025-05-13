package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/metrics")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
})
@Tag(name = "Metrics", description = "Endpoints for accessing various metrics and statistics")
public class MetricsController {

    @GetMapping("/advanced")
    @Operation(summary = "Statistics calculated from the data obtained (assuming general or filterable metrics).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved advanced metrics")
    })
    public ResponseEntity<Object> getAdvancedMetrics(){
        return ResponseEntity.ok(null);
    }

}
