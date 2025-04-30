package unq.dapp.grupoj.soccergenius.exceptions;

public class AlreadyUsedEmail extends RuntimeException {
    public AlreadyUsedEmail(String message) {
        super("The email " + message + " is already in use.");
    }
}
