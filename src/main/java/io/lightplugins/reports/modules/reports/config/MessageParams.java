package io.lightplugins.reports.modules.reports.config;

import io.lightplugins.reports.util.manager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageParams {

    private final FileConfiguration config;

    public MessageParams(FileManager selectedLanguage) {
        this.config = selectedLanguage.getConfig();
    }

    public String prefix() { return config.getString("prefix"); }
    public String noPermission() { return config.getString("noPermission"); }
    public String moduleReload() { return config.getString("moduleReload"); }
    public String wrongSyntax() { return config.getString("wrongSyntax"); }
    public String cannotReportSelf() { return config.getString("cannotReportSelf"); }
    public String playerNotFound() { return config.getString("playerNotFound"); }
    public String reportSuccess() { return config.getString("reportSuccess"); }
    public String invalidReason() { return config.getString("invalidReason"); }
    public String onCooldown() { return config.getString("onCooldown"); }
    public String alreadyReported() { return config.getString("alreadyReported"); }
}

