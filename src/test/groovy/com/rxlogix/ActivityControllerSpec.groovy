package com.rxlogix

import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
class ActivityControllerSpec extends Specification implements ControllerUnitTest<ActivityController>  {

    ActivityService activityService

    void setup() {
        activityService = Mock(ActivityService)
        controller.activityService = activityService
    }

    void "test listByExeConfig with valid executedIdList and appType"() {
        given: "A valid executedIdList and appType"
        Set executedIdList = ["1", "2", "3"]
        params.executedIdList = executedIdList.join(",")
        params.appType = "testApp"
        controller.userService.getUser() >> new User(username: "testUser")

        controller.activityService.listActivitiesByExConfig(_, _) >> [[activity_id: 1], [activity_id: 2], [activity_id: 3]]

        when: "activityService.listActivitiesByExConfig is called"
        controller.listByExeConfig()

        then: "The response status is 200"
        response.status == 200
        and: "The respons contains 3 activities in JSON format"
        response.json.size() == 3
    }

    void "test listByExeConfig with no executedIdList"() {
        given: "No executedIdList parameter"
        params.executedIdList = null
        params.appType = "testApp"

        controller.activityService.listActivitiesByExConfig(_, _) >> []

        when: "listByExeConfig is called"
        controller.listByExeConfig()

        then: "The response status is 200"
        response.status == 200
        and: "The response is an empty list in JSON format"
        response.json.size() == 0
    }

    void "test listByExeConfig with no appType"() {
        given: "No appType parameter"
        Set executedIdList = ["1", "2", "3"]
        params.executedIdList = executedIdList.join(",")
        params.appType = null

        controller.activityService.listActivitiesByExConfig(_, _) >> []

        when: "listByExeConfig is called"
        controller.listByExeConfig()

        then: "The response status is 200"
        response.status == 200
        and: "The response is an empty list in JSON format"
        response.json.size() == 0
    }
}