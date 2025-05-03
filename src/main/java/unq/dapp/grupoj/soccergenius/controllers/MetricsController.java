package unq.dapp.grupoj.soccergenius.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @GetMapping("/advanced")
    @Operation(summary = "Estadísticas calculadas a partir de los datos obtenidos. (Asumiendo métricas generales o que se pueden filtrar).")
    public ResponseEntity<Object> getAdvancedMetrics(){
        return ResponseEntity.ok(null);
    }

}
