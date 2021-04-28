package no.cantara.config;

import java.util.List;

public class DebugUtils {

    public static String debugSources(List<ApplicationProperties.Source> sources, boolean debugOverriddenSources) {
        StringBuilder sb = new StringBuilder();
        debugSources(sources, debugOverriddenSources, sb);
        return sb.toString();
    }

    public static void debugSources(List<ApplicationProperties.Source> sources, boolean debugOverriddenSources, StringBuilder sb) {
        if (sources.isEmpty()) {
            return;
        }
        int n = 1;
        if (debugOverriddenSources) {
            n = sources.size();
        }
        for (int i = 0; i < n; i++) {
            sb.append("- ");
            for (int j = 0; j < i * 4; j++) {
                sb.append(" ");
            }
            if (i == 0) {
                sb.append("effective-source: ");
            } else {
                sb.append("other-source: ");
            }
            debugSource(sources.get(i), sb);
        }
    }

    public static String debugSource(ApplicationProperties.Source source) {
        StringBuilder sb = new StringBuilder();
        debugSource(source, sb);
        return sb.toString();
    }

    public static void debugSource(ApplicationProperties.Source source, StringBuilder sb) {
        sb.append("property: ").append(source.propertyName()).append("='").append(source.propertyValue())
                .append("' -- by source: ").append(source.description())
                .append(" -- configured at: ").append(source.stackTraceElement().getCodeLocation())
                .append("\n");
    }
}
