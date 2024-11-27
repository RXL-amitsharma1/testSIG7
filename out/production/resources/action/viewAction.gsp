<%@ page import="com.rxlogix.config.Meeting" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Alert.title" args="${[actionInstance.type]}"/></title>
</head>

<body>

<rx:container title="ACTION DETAILS">

    <div class="row rxmain-container-content">
        <div class="container-fluid">
            <div class="row">
                <div class="col-sm-4">
                    <label>Type</label>

                    <p>${actionInstance.type}</p>
                </div>

                <div class="col-sm-4">
                    <label>Status</label>

                    <p>${actionInstance.actionStatus}</p>
                </div>

                <div class="col-sm-4">
                    <label>Assigned To</label>

                    <p id="assignedTo">${actionInstance.assignedTo?.fullName}</p>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-4">
                    <label>Due Date</label>

                    <p><g:formatDate type="date" style="MEDIUM" date="${actionInstance.dueDate}"/></p>
                </div>


                <div class="col-xs-4">
                    <label>Meeting</label>

                    <p>
                        <g:if test="${actionInstance.meetingId}">
                            <g:link controller="meeting" action="viewMeeting" params="[id: actionInstance.meetingId]"
                                    target="_blank">
                                ${Meeting.get(actionInstance.meetingId)?.createMeetingTitleText()}
                            </g:link>
                        </g:if>
                        <g:else>
                            -
                        </g:else>
                    </p>
                </div>

                <div class="col-sm-4">
                    <label><g:message code="app.label.scheduledBy"/></label>

                    <p>${actionInstance.owner.fullName}</p>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-12">
                    <label><g:message code="app.label.description"/></label>

                    <p>${actionInstance.details}</p>
                </div>

            </div>

            <div class="row">
                <div class="col-sm-12">
                    <label>Comments</label>

                    <p>${actionInstance.comments}</p>
                </div>
            </div>
        </div>
    </div>
</rx:container>
</body>
</html>