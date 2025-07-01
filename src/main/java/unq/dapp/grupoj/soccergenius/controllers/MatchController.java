package unq.dapp.grupoj.soccergenius.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unq.dapp.grupoj.soccergenius.services.matches.MatchService;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/prediction")
    public ResponseEntity<String> getMatchPredictionBetween(@RequestParam String teamAName, @RequestParam String teamBName) {
        return ResponseEntity.ok(matchService.getMatchPredictionBetween(teamAName, teamBName));
    }
}
