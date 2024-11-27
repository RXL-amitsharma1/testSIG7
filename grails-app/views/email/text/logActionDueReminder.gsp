<%@ page contentType="text/html" %>

<g:message code='app.action.due.reminder.msg'/>
Topic Name: ${alertInstance.topic}
Detected by: ${alertInstance.detectedBy}
Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)}
Workflow State: ${alertInstance.workflowState?.displayName}
Initial data source: ${alertInstance.initialDataSource}
Additional information:${alertInstance.description}
Assigned user: ${alertInstance.assignedTo.fullName}
Disposition: ${alertInstance.disposition?.displayName}
Priority: ${alertInstance.priority?.displayName}
Alert Detail Screen Hyperlink: ${alertLink}



