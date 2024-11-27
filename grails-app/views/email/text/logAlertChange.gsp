<%@ page contentType="text/plain" %>

<g:if test="${!currentState.equals('none')}">
    <g:message code='app.disposition.changed.msg'/>&nbsp;${currentState} to ${newState}
</g:if>
<g:if test="${!currentDisposition.equals('none')}">
    <g:message code='app.disposition.changed.msg'/>&nbsp;${currentDisposition} to ${newDisposition}
</g:if>
Topic Name: ${alertInstance?.topic}
Detected by: ${alertInstance.detectedBy}
Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)}
Initial data source: ${alertInstance.initialDataSource}
Additional information:${alertInstance.description}
Assigned user : ${fullName}
Alert Detail Screen Hyperlink: ${alertLink}
    


