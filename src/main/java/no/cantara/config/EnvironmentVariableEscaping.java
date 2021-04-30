package no.cantara.config;

public class EnvironmentVariableEscaping {

    public static String unescape(String envVarKey) {
        String propKey = envVarKey;
        propKey = propKey.replaceAll("_d_", "-");
        propKey = propKey.replaceAll("(?<!_u)_(?!u_)", "."); // a underscore, not preceeded by _u (negative lookbehind), and not followed by u_ (negative lookahead)
        propKey = propKey.replaceAll("_u_", "_");
        propKey = propKey.toLowerCase();
        return propKey;
    }

    public static String escape(String propKey) {
        String envVarKey = propKey;
        envVarKey = envVarKey.toUpperCase();
        envVarKey = envVarKey.replaceAll("[_]", "_u_");
        envVarKey = envVarKey.replaceAll("[.]", "_");
        envVarKey = envVarKey.replaceAll("[-]", "_d_");
        return envVarKey;
    }
}
