package io.lightplugins.reports.modules.reports.config;

import io.lightplugins.reports.modules.reports.LightReports;

import java.util.List;

public class SettingParams {

    private final LightReports lightReports;

    public SettingParams(LightReports lightReports) {
        this.lightReports = lightReports;
    }

    public String getModuleLanguage() {
        return lightReports.getSettings().getConfig().getString("module-language");
    }

    public DefaultWrapper defaultWrapper() { return new DefaultWrapper(); }

    public class DefaultWrapper {

        public List<String> getReportReasons() {
            return lightReports.getSettings().getConfig().getStringList("report-reasons");
        }
        public int getCooldownTime() {
            return lightReports.getSettings().getConfig().getInt("report-cooldown");
        }
    }
}
