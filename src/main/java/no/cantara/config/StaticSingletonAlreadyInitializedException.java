package no.cantara.config;

public class StaticSingletonAlreadyInitializedException extends RuntimeException {
    public StaticSingletonAlreadyInitializedException(String message) {
        super(message);
    }
}
