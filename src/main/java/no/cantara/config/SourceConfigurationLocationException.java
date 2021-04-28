package no.cantara.config;

public class SourceConfigurationLocationException extends RuntimeException {

    final int significantCodeLocationStackIndex;

    public SourceConfigurationLocationException(int significantCodeLocationStackIndex) {
        this.significantCodeLocationStackIndex = significantCodeLocationStackIndex;
    }

    public String getCodeLocation() {
        StackTraceElement[] stackTrace = getStackTrace();
        StackTraceElement element = stackTrace[significantCodeLocationStackIndex];
        return element.toString();
    }
}
