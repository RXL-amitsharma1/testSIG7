<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Alert.title" args="${[meetingInstance.meetingTitle]}"/></title>
</head>

<body>
<rx:container title="MEETING DETAILS">
            <div class="row rxmain-container-content">
                <div class="container-fluid">
                    <div class="col-xs-12">
                        <div class="row">
                            <div class="col-xs-3">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>Meeting Title</label>
                                        <div>${meetingInstance.meetingTitle}</div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>Meeting Date</label>
                                        <div><g:formatDate type="date" style="MEDIUM" date="${meetingInstance.meetingDate}"></g:formatDate></div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-3">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label>Meeting Owner</label>
                                                <div id="assignedTo">${meetingInstance.meetingOwner?.fullName}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>Duration</label>

                                        <div>${meetingInstance.duration}&nbsp;
                                        <g:if test="${meetingInstance.duration > 60}">
                                            Hrs
                                        </g:if>
                                        <g:else>
                                            Mins
                                        </g:else>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-3">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>Meeting Attendees</label>
                                        <g:each in="${meetingInstance.attendees}" var="attendee">
                                            <div>${attendee.fullName}</div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-3">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <label>Last Modified</label>
                                        <div><g:formatDate type="date" style="MEDIUM" date="${meetingInstance.lastUpdated}"></g:formatDate></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
</rx:container>
</body>
</html>