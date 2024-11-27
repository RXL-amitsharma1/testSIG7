databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321339-346") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM ACTIVITY_TYPE  WHERE VALUE = 'MeetingCanceled';")
            }
        }
        grailsChange {
            change {
                try {
                    String updateActivityTypeQuery = "BEGIN " +
                            "UPDATE ACTIVITY_TYPE SET VALUE = \'MeetingCancelled\' WHERE VALUE = \'MeetingCanceled\'; " +
                            "END; "
                    sql.execute(updateActivityTypeQuery)
                }catch(Exception ex) {
                    println("##################### Error occurred while updating ACTIVITY_TYPE table. #############")
                    ex.printStackTrace()
                }
            }
        }
    }
}