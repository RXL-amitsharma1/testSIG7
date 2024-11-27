<%@ page contentType="text/html"%>

<g:message code='app.email.alert.create.msg' args='${[alertInstance.initialDataSource]}' />
Product Name: ${productName}
Topic Name: ${alertInstance.topic}
Detected by: ${alertInstance.detectedBy}
Detected date:${com.rxlogix.util.DateUtil.toUSDateString(alertInstance.detectedDate, timezone)}
Additional information:${alertInstance.description}
Assigned user : ${alertInstance.assignedTo.fullName}
Alert Detail Screen Hyperlink: ${alertLink}
    


