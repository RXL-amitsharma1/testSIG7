package com.rxlogix.helper

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.user.User
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by Lei Gao on 1/8/16.
 */
@Ignore
class AlertHelperSpec extends Specification {
    def alert

    def setup() {
        def user1 = new User(username: "user1")
        user1.metaClass.getFullName = {"Jane Doe"}
        def user2 = new User(username: "user2")
        user2.metaClass.getFullName = {"John Doe"}

        alert = new AdHocAlert(name: "test alert", productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN"}],"4":[],"5":[]}')
        alert.alertRmpRemsRef = "test ref"
        alert.detectedDate = new DateTime(2016, 1, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate()
        alert.assignedTo = user1
        alert.detectedBy = "detectedBy"
    }

    def "composeDetailRowsForDisplay" () {
        when:
            def result = AlertHelper.composeDetailRowsForDisplay(alert, 'America/Los_Angeles')

        then:
            result.toString() == "[[[Alert Details, name, test alert, left, 2], " +
                    "[Product/Generic Name, productSelection, IBUPROFEN, left, 2], " +
                    "[Topic Name, topic, null, left, 2], [Event, eventSelection, , left, 2], " +
                    "[Detected By, detectedBy, detectedBy, left, 2], " +
                    "[Detected Date, detectedDate, 15-Jan-2016, left, 2]], " +
                    "[[Initial Datasource, initialDataSource, null, left, 2], " +
                    "[No of ICSR, numberOfICSRs, null, left, 2], " +
                    "[Issue Previously Tracked, issuePreviouslyTracked, null, left, 2], " +
                    "[Population Specific, populationSpecific, null, left, 2], " +
                    "[Report Type, reportType, null, left, 2], " +
                    "[RMP/REMS Ref, alertRmpRemsRef, test ref, left, 2]], " +
                    "[[Indication, indication, null, left, 2], [Formulation, formulations, null, left, 2], " +
                    "[Device Related, deviceRelated, No, left, 2], " +
                    "[Country, countryOfIncidence, null, left, 2], " +
                    "[Assigned To, assignedTo, Jane Doe, left, 2], " +
                    "[Public, publicAlert, No, left, 2]], " +
                    "[[Shared with Group, sharedWithGroups, null, left, 2], " +
                    "[Population Specific, populationSpecific, null, left, 2], " +
                    "[Comments, notes, null, left, 6], " +
                    "[Evaluation Methods, evaluationMethods, null, left, 2]], " +
                    "[[Description, description, null, left, 12]]]"
    }
}
