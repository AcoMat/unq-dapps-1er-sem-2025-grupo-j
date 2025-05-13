package unq.dapp.grupoj.soccergenius.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
public class MatchController {

    @GetMapping("/prediction")
    public ResponseEntity<?> getMatchPredictionBetween(@RequestParam String team1Id, @RequestParam String team2Id) {
        return ResponseEntity.ok("Predicción entre los equipos " + team1Id + " y " + team2Id);
    }
}
