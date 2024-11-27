<%@ page import="java.text.SimpleDateFormat" %>
Import Alert for ${new SimpleDateFormat('MM-dd-yyy').format(logInstance.startTime)}
Start Time: ${new SimpleDateFormat("MM-dd-yyy 'at' HH:mm:ss z").format(logInstance.startTime)}
End Time: ${new SimpleDateFormat("MM-dd-yyy 'at' HH:mm:ss z").format(logInstance.endTime)}
Number succeeded: ${logInstance.numSucceeded}
Number failed: ${logInstance.numFailed}
    


