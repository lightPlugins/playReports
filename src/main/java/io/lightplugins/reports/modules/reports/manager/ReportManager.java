package io.lightplugins.reports.modules.reports.manager;

import io.lightplugins.reports.modules.reports.LightReports;
import io.lightplugins.reports.modules.reports.enums.ReportStatus;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Date;
import java.util.UUID;

@Getter
public class ReportManager {

    private final UUID reportID;
    private final UUID reporter;
    private final UUID reported;
    private final String reason;
    private UUID resolvedBy;
    private ReportStatus status;
    @Setter
    private Date createDate;
    @Setter
    private Date solveDate;

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
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".created", System.currentTimeMillis());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".solved", 0);

        this.createDate = new Date(System.currentTimeMillis());

        LightReports.instance.getStorage().saveConfig();

    }

    // delete file entry for report
    public void deleteReportEntry() {

        LightReports.instance.getStorage().getConfig().set("reports." + reportID, null);
        LightReports.instance.getStorage().saveConfig();

    }

    // update file entry for report

    public void setSolved(UUID resolvedBy, ReportStatus reportStatus) {

        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".resolvedBy", resolvedBy.toString());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".status", reportStatus.getType());
        LightReports.instance.getStorage().getConfig().set("reports." + reportID + ".solved", System.currentTimeMillis());

        this.resolvedBy = resolvedBy;
        this.solveDate = new Date(System.currentTimeMillis());
        this.status = reportStatus;
        this.solveDate = new Date(System.currentTimeMillis());

        LightReports.instance.getStorage().saveConfig();

    }

    public String getReportedName() {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(reported);
        return offlinePlayer == null ? "Unknown" : offlinePlayer.getName();
    }

}
