package com.rxlogix

class UserDashboardCounts {

    Long userId
    String userDispCaseCounts
    String groupDispCaseCounts
    String userDueDateCaseCounts
    String groupDueDateCaseCounts
    String userDispPECounts
    String groupDispPECounts
    String userDueDatePECounts
    String groupDueDatePECounts

    static constraints = {
        userDispCaseCounts nullable: true
        groupDispCaseCounts nullable: true
        userDueDateCaseCounts nullable: true
        groupDueDateCaseCounts nullable: true
        userDispPECounts nullable: true
        groupDispPECounts nullable: true
        userDueDatePECounts nullable: true
        groupDueDatePECounts nullable: true
    }

    static mapping = {
        id name: 'userId',generator: 'assigned'
        version false
        userDispCaseCounts sqlType: "varchar2(4000 CHAR)"
        groupDispCaseCounts sqlType: "varchar2(4000 CHAR)"
        userDueDateCaseCounts sqlType: "varchar2(8000 CHAR)"
        groupDueDateCaseCounts sqlType: "varchar2(8000 CHAR)"
        userDispPECounts sqlType: "varchar2(4000 CHAR)"
        groupDispPECounts sqlType: "varchar2(4000 CHAR)"
        userDueDatePECounts sqlType: "varchar2(8000 CHAR)"
        groupDueDatePECounts sqlType: "varchar2(8000 CHAR)"
    }
}
