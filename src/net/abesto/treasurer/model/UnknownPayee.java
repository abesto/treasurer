package net.abesto.treasurer.model;

public class UnknownPayee extends Model {
    private String payee;

    public UnknownPayee(String payee) {
        this.payee = payee;
    }

    public String getPayee() {
        return payee;
    }
}

