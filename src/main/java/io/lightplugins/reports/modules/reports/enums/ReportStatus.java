package io.lightplugins.reports.modules.reports.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {

    OPEN("open"),
    ACCEPTED("accepted"),
    REJECTED("rejected")
    ;

    private final String type;
    ReportStatus(String type) { this.type = type; }

}
