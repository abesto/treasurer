package net.abesto.treasurer.model;

public class FailedToParseSms extends Model {
    private String message;

    public FailedToParseSms(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
