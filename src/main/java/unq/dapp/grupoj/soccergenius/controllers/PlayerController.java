package unq.dapp.grupoj.soccergenius.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class PlayerController {


    @GetMapping("/performance/{playerId}")
    public ResponseEntity<>
}
