package net.abesto.treasurer.parsers;

public class ParserFactory {
    private static ParserFactory instance;
    private ParserFactory() {}
    public static ParserFactory getInstance() {
        if (instance == null) {
            instance = new ParserFactory();
        }
        return instance;
    }

    public SmsParser buildFromConfig() {
        // Will build a parser based on settings once said settings exist
        return new OTPCreditCardPaymentParser();
    }
}
