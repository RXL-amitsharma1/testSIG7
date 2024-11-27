package com.rxlogix.enums

public enum SignalSummaryReportSectionsEnum {

    OVERALL_SUMMARY('Overall Summary'),
    CASES_PEC_INFORMATION('Safety Observations'),
    COMMENTS('Comments'),
    ACTIONS_TAKEN('Actions'),
    MEETING_MINUTES('Meetings'),
    ATTACHED_DOCUMENTS('Attached Documents'),
    APPENDIX('Appendix'),
    SIGNAL_INFORMATION('Signal Information'),
    RMMS('RMMs'),
    COMMUNICATION('Communication'),
    REFERENCES('References'),
    WORKFLOW_LOG('WorkFlow Log')
    private final String val

    SignalSummaryReportSectionsEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.signal.summary.report.section.${this.name()}"
    }

    static List getDefaultSections() {
        [SIGNAL_INFORMATION ,WORKFLOW_LOG ,CASES_PEC_INFORMATION, REFERENCES, ACTIONS_TAKEN, MEETING_MINUTES, RMMS, COMMUNICATION]
    }
}
