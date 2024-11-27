<%@ page contentType="text/plain" %>

<g:message code='app.document.validation.reminder.msg'/>
Produce/Generic Name: ${alertInstance.buildProductNameList().toString()}
Topic: ${alertInstance.topic}
Detected by: ${alertInstance.detectedBy}
Detected date: ${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)}
Workflow state: ${alertInstance.workflowState?.displayName}
Initial data source: ${alertInstance.initialDataSource}
Additional information:${alertInstance.description}
Assigned user: ${alertInstance.assignedTo.fullName}
Disposition: ${alertInstance.disposition?.displayName}
Priority: ${alertInstance.priority?.displayName}
Target completion date: ${com.rxlogix.util.DateUtil.toUSDateString(document?.targetDate, timezone)}
Document status: ${document?.documentStatus}
Document status date: ${com.rxlogix.util.DateUtil.toUSDateString(document?.statusDate, timezone)}

Alert Detail Screen Hyperlink: ${alertLink}
