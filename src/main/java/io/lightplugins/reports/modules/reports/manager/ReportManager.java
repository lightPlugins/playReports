package io.lightplugins.reports.modules.reports.manager;

import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReportManager {

    private final UUID reportID;
    private final UUID reporter;
    private final UUID reported;
    private final String reason;
    private final UUID resolvedBy;
    private final ReportStatus status;

    public ReportManager(
            UUID reportID,
            UUID reporter,
            UUID reported,
            String reason,
            UUID resolvedBy,
            ReportStatus status) {

        this.reportID = reportID;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.resolvedBy = resolvedBy;
        this.status = status;
    }


    // create file entry for report
    public void createNewReportEntry() {

        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".reporter", reporter.toString());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".reported", reported.toString());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".reason", reason);
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".resolvedBy",
                resolvedBy == null ? "" : resolvedBy.toString());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".status", status.getType().toUpperCase());
        LightReports.instance.getStorage().saveConfig();

    }

    // delete file entry for report
    public void deleteReportEntry() {

        LightReports.instance.getStorage().getConfig().set("reports." + reportID, null);
        LightReports.instance.getStorage().saveConfig();

    }

    // update file entry for report

    public void setSolved(UUID resolvedBy, ReportStatus reportStatus) {

        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".solvedBy", resolvedBy.toString());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".status", reportStatus.getType());
        LightReports.instance.getStorage().saveConfig();

    }

}
