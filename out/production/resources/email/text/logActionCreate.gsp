<%@ page contentType="text/plain" %>

<g:message code='app.email.action.create.msg' args='${[productName]}'/>
Product Name: ${productName}
Topic Name: ${alertInstance.topic}
Detected by: ${alertInstance.detectedBy}
Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)}
Workflow State: ${alertInstance.workflowState?.displayName}
Initial data source: ${alertInstance.initialDataSource}
Additional information:${alertInstance.description}
Assigned user: ${alertInstance.assignedTo.fullName}
Disposition: ${alertInstance.disposition?.displayName}
Priority: ${alertInstance.priority?.displayName}
Action: ${action?.config.displayName}
Targeted completion date: ${com.rxlogix.util.DateUtil.toUSDateString(action?.dueDate, timezone)}
Alert Detail Screen Hyperlink: ${alertLink}
    


