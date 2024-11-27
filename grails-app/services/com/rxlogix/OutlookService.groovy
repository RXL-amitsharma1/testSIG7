package com.rxlogix

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.exception.PVSException
import com.rxlogix.outlook.IdToken
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders
import groovy.time.TimeCategory
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.joda.time.DateTimeZone

import java.text.SimpleDateFormat

class OutlookService {

    def userService
    def grailsApplication

    String getAccessToken() {
        userService.getUser().outlookAccessToken
    }

    String getRefreshToken() {
        userService.getUser().outlookRefreshToken
    }

    Map getOutlookConfig() {
        Holders.config.outlook
    }

    Map authorize(String code, String idToken, String expectedNonce) {
        log.info("authorize called")
        Map responseMap = [status: true, message: 'authorized successfully']
        IdToken idTokenObj = IdToken.parseEncodedToken(idToken, expectedNonce);
        if (idTokenObj != null) {
            Map tokenMap = getTokenFromAuthCode(idTokenObj.getTenantId(), "authorization_code", code);
            User user = userService.saveOutlookData(tokenMap.access_token, tokenMap.refresh_token)
            Map userMap
            try {
                userMap = getCurrentUser(user.outlookAccessToken)
            } catch (IOException e) {
                responseMap.status = false
                responseMap.message = e.getMessage()
            }
        } else {
            responseMap.status = false
            responseMap.message = "ID token failed validation."
        }
        responseMap
    }

    Map getTokenFromAuthCode(String tenantId, String grantType, String authCode) {
        log.info("getTokenFromAuthCode called")
        Map postBody = [
                client_id    : outlookConfig.appId,
                client_secret: outlookConfig.secret,
                grant_type   : grantType,
                code         : authCode,
                redirect_uri : outlookConfig.redirectUrl
        ]

        def restClient = new RESTClient("${outlookConfig.authorizeUrl}/${tenantId}/oauth2/v2.0/token")
        def response = restClient.post(
                body: postBody,
                requestContentType: ContentType.URLENC)
        response.responseData
    }

    Map getCurrentUser(String accessToken) {
        log.info("getCurrentUser called")
        def restClient = new RESTClient("${outlookConfig.apiUrl}/api/v2.0/me")
        def response = restClient.get(
                headers: getHeaderParams(accessToken)
        )
        response.responseData
    }

    Map getHeaderParams(String accessToken) {
        [
                'User-Agent'              : 'pvsignal',
                "client-request-id"       : UUID.randomUUID().toString(),
                "return-client-request-id": true,
                "Authorization"           : String.format("Bearer %s", accessToken)
        ]
    }

    def createMeeting() {
        def ret = [:]
        Map postBody = [
                Subject: "Demo of Outlook Api",
                Body   : [
                        ContentType: "HTML",
                        Content    : "Rx Testing"
                ],
                Start  : [
                        DateTime: "2017-06-07T14:00:00",
                        TimeZone: "India Standard Time"
                ]
                ,
                End    : [
                        DateTime: "2017-06-07T15:00:00",
                        TimeZone: "India Standard Time"
                ]
        ]
// initialze a new builder and give a default URL
        def http = new HTTPBuilder('https://outlook.office.com')

        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = 'https://outlook.office.com/api/v2.0/me/events/'
            headers.Authorization = "Bearer ${accessToken}"
            body = postBody

            response.success = { resp, reader -> ret = [status: 200, result: reader] }
            response.failure = { resp -> ret = [status: 500] }
        }
    }


    void validateFindMeetingTimesParams(params) throws PVSException{
        if(!params.meetingAttendees){
            throw new PVSException("meeting attendees is required.")
        }
    }

    Map createFindMeetingTimesRequestData(def params) {
        def requestedAttendees = params.meetingAttendees.split(",")
        List<User> attendeeList = []
        requestedAttendees.each {
            attendeeList << User.get(Long.parseLong(it))
        }
        String timezone = userService.getUser().preference.timeZone
        DateTimeZone dz = DateTimeZone.forID(timezone)
        String timeZoneDisplayName = dz.toTimeZone().getDisplayName()
        log.info("Timezone Display Name : ${timeZoneDisplayName}")
        Map outlookTimeZones = Holders.config.timezone.mappings
        if (outlookTimeZones[timeZoneDisplayName]) {
            timeZoneDisplayName = outlookTimeZones[timeZoneDisplayName]
        }
        List attendeePayload = attendeeList.collect {
            [
                    EmailAddress: [
                            Name   : it.fullName,
                            Address: it.email
                    ],
                    Type        : 'Required'
            ]
        }
        Map postBody = [
                Attendees              : attendeePayload,
                TimeConstraint         : [
                        ActivityDomain: "Work",
                        Timeslots     : [
                                [
                                        "Start": [
                                                "DateTime": parseMeetingTimesDate(params.fmtStartDate),
                                                "TimeZone": timeZoneDisplayName
                                        ],
                                        "End"  : [
                                                "DateTime": parseMeetingTimesDate(params.fmtEndDate, true),
                                                "TimeZone": timeZoneDisplayName
                                        ]
                                ]
                        ]
                ],
                MeetingDuration        : 'PT1H',
                ReturnSuggestionReasons: true
        ]

        log.info("post body = ${postBody}")
        postBody
    }

    ResponseDTO findMeetingTimes(params) {
        String timezone = userService.getUser().preference.timeZone
        log.info("********** Outlook Api Call - Start **********")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        String url = outlookConfig.apiUrl + '/api/v2.0/me/findmeetingtimes/'
        Map ret = [:]
        try{
            validateFindMeetingTimesParams(params)
            Map postBody = createFindMeetingTimesRequestData(params)
            def http = new HTTPBuilder(outlookConfig.apiUrl)
            int statusCode
            http.request(Method.POST, ContentType.JSON) {
                uri.path = url
                headers = getHeaderParams(getAccessToken())
                body = postBody

                Map responseBase = [:]
                // response handlers
                response.success = { resp, reader ->
                    responseDTO.status = true
                    statusCode = resp.getStatus()
                    ret = [status: 200, result: reader]
                }
                response.failure = { resp ->
                    log.info("failure response : ${resp.responseBase}")
                    responseDTO.status = false
                    statusCode = resp.getStatus()
                }
            }
            log.info("Status Code : ${statusCode}")
            if (!responseDTO.status) {
                String message = (statusCode == 401) ? "Outlook session is expired. Please login in outlook to continue." : "Some error occurred"
                throw new PVSException(message)
            }
            Map apiResponse = ret.result
            log.info("Api Response : ${apiResponse}")
            Date parsedStartDate
            Date parsedEndDate
            if (apiResponse) {
                List response = []
                List attendees = []
                List attendeesAvailability = []
                Map timeSlot
                List meetingTimeSuggestions = apiResponse.get('MeetingTimeSuggestions') as List
                if (meetingTimeSuggestions) {
                    meetingTimeSuggestions.each {
                        attendeesAvailability = it.AttendeeAvailability
                        attendees = attendeesAvailability.collect {
                            [email: it.get('Attendee').EmailAddress.Address]
                        }
                        timeSlot = it.MeetingTimeSlot
                        parsedStartDate = DateUtil.parseDate(timeSlot.Start.DateTime, "yyyy-MM-dd'T'HH:mm:ss")
                        parsedEndDate = DateUtil.parseDate(timeSlot.End.DateTime, "yyyy-MM-dd'T'HH:mm:ss")
                        timeSlot = [startDateTime: DateUtil.toDateStringWithTime(parsedStartDate, timezone),
                                    endDateTime  : DateUtil.toDateStringWithTime(parsedEndDate, timezone)]

                        response << [attendees: attendees, timeSlot: timeSlot, startDate: parsedStartDate]
                    }
                }
                response = response.sort { it.startDate }
                responseDTO.data = [meetingTimeSuggestions: response.take(5)]
            }
        }catch (PVSException e){
            responseDTO.status = false
            responseDTO.message = e.message
        }
        log.info("********** Outlook Api Call - End **********")
        responseDTO
    }

    def refreshToken() {
        def ret = [:]
        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"
        def http = new HTTPBuilder(url)
        Map postBody = [
                client_secret: outlookConfig.secret,
                grant_type   : 'refresh_token',
                refresh_token: refreshToken,
                client_id    : outlookConfig.appId
        ]

        http.request(Method.POST) {
            uri.path = url
            requestContentType = ContentType.URLENC
            body = postBody

            // response handlers
            response.success = { resp, reader -> ret = [status: 200, result: reader] }
            response.failure = { resp -> ret = [status: 500] }
        }

    }

    String parseMeetingTimesDate(String requestedDate, Boolean isEndDate = false) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        Date date = (Date) dateFormat.parse(requestedDate)
        if (isEndDate) {
            use(TimeCategory) {
                date = date + 1.day - 1.second
            }
        }
        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        newDateFormat.format(date)
    }

}
