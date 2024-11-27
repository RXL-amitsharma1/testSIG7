package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ProductGroup
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertReviewCompleted
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.codec.digest.DigestUtils
import org.grails.datastore.mapping.model.PersistentEntity
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.validation.ObjectError

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.text.DateFormat
import java.text.Format
import java.text.SimpleDateFormat
import java.util.regex.Pattern

class MiscUtil {
    def static md5ChecksumForFile(String filePath) {
        if (!filePath) null
        FileInputStream fis = new FileInputStream(new File(filePath));
        def checksum = md5ChecksumForFile fis
        fis.close()

        checksum
    }

    def static md5ChecksumForFile(FileInputStream fis) {
        DigestUtils.md5Hex(fis);
    }

    def static calcDueDate(def alert, def priority, Disposition disposition = null, boolean resetDate = false, List dispositionConfigs=[], boolean isAggregate = false) {
        // TODO change the following two lines
        if (!alert || !priority)
            return
        String serverTimeZone = Holders.config.server.timezone
        Boolean isNewDetectedDate = false
        if(alert instanceof ValidatedSignal && alert.newDetectedDate != null) { // use case - 7
            //newDetectedDate
            isNewDetectedDate = true
        }
        def theDetectedDate = new DateTime(isNewDetectedDate?alert.newDetectedDate:alert.detectedDate, DateTimeZone.forID(serverTimeZone))
        Integer reviewPeriod = priority.reviewPeriod
        Integer dispReviewPeriod = null
        Boolean isChangeDueDate = true
        if(disposition){
            dispReviewPeriod = dispositionConfigs?.find {it -> it.disposition == disposition}?.reviewPeriod
            if(dispReviewPeriod) {
                reviewPeriod = dispReviewPeriod
            } else if(resetDate && alert.dueDate != null){
                isChangeDueDate = false
            }
        }
        def reviewDate = new DateTime(new Date(),DateTimeZone.forID(serverTimeZone))
        if(resetDate) {
            alert.reviewDate = reviewDate.toDate()
        }
        def theDueDate = resetDate ? reviewDate.plusDays(reviewPeriod) : theDetectedDate.plusDays(reviewPeriod)
        if (alert instanceof ValidatedSignal) {
            alert.dueDate = alert.isDueDateUpdated ? alert.actualDueDate  : theDueDate.toDate()
        } else {
            alert.dueDate = isChangeDueDate ? theDueDate.toDate() : alert.dueDate
        }
        if (isAggregate) {
            alert.dueDate = theDetectedDate.plusDays(reviewPeriod).toDate()
        }
        if(disposition && resetDate && (disposition.reviewCompleted || disposition.closed) && !dispReviewPeriod){
            alert.dueDate = null
            switch (alert.getClass().getSimpleName()) {
                case 'SingleCaseAlert':
                    SingleCaseAlert.executeUpdate("update SingleCaseAlert set dueDate=null where caseNumber=:caseNo and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [caseNo: alert.caseNumber, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    ArchivedSingleCaseAlert.executeUpdate("update ArchivedSingleCaseAlert set dueDate=null where caseNumber=:caseNo and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [caseNo: alert.caseNumber, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    break
                case 'AggregateCaseAlert':
                    AggregateCaseAlert.executeUpdate("update AggregateCaseAlert set dueDate=null where productName=:prod and pt=:pt and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [prod: alert.productName, pt: alert.pt, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    ArchivedAggregateCaseAlert.executeUpdate("update ArchivedAggregateCaseAlert set dueDate=null where productName=:prod and pt=:pt and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [prod: alert.productName, pt: alert.pt, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    break
                case 'EvdasAlert':
                    EvdasAlert.executeUpdate("update EvdasAlert set dueDate=null where substance=:sub and pt=:pt and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [sub: alert.substance, pt: alert.pt, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    ArchivedEvdasAlert.executeUpdate("update ArchivedEvdasAlert set dueDate=null where substance=:sub and pt=:pt and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [sub: alert.substance, pt: alert.pt, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    break
                case 'AdHocAlert':
                    AdHocAlert.executeUpdate("update AdHocAlert set dueDate=null where productSelection=:prod and eventGroupSelection=:pt and alertConfiguration=:config and executedAlertConfiguration!=:exConfig ",
                            [prod: alert.productSelection, pt: alert.eventGroupSelection, config: alert.alertConfiguration, exConfig: alert.executedAlertConfiguration])
                    break
            }

        }
        alert.dueDate
    }

    def static calcDueDateForDueInStartPoint(alert, priority, Disposition disposition = null, boolean resetDate = false, List dispositionConfigs=[]) {
        // TODO change the following two lines
        if (!alert || !priority)
            return
        String serverTimeZone = Holders.config.server.timezone
        Boolean isSignalWithDefaultDisp = false
        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
        Date dueInStartBaseDate = alert.signalStatusHistories.find { it.signalStatus == dueInStartPoint }?.dateCreated
        isSignalWithDefaultDisp = alert.signalStatusHistories.find { it.isAutoPopulate == true }?.isAutoPopulate
        def theDueInStartBaseDate = dueInStartBaseDate ? new DateTime(dueInStartBaseDate, DateTimeZone.forID(serverTimeZone)) : null
        Integer reviewPeriod = priority.reviewPeriod
        Integer dispReviewPeriod = null
        Boolean isChangeDueDate = true
        if(disposition){
            dispReviewPeriod = dispositionConfigs?.find {it -> it.disposition == disposition}?.reviewPeriod
            if(dispReviewPeriod) {
                reviewPeriod = dispReviewPeriod
            } else if(resetDate && alert.dueDate != null){
                isChangeDueDate = false
            }
        }
        def reviewDate = new DateTime(theDueInStartBaseDate,DateTimeZone.forID(serverTimeZone))
        if(resetDate) {
            alert.reviewDate = reviewDate.toDate()
        }
        def theDueDate = resetDate ?  new DateTime(new Date(),DateTimeZone.forID(serverTimeZone)).plusDays(reviewPeriod) : theDueInStartBaseDate?.plusDays(reviewPeriod)
        if (alert instanceof ValidatedSignal) {
            alert.dueDate = (alert.isDueDateUpdated || resetDate ) ? alert.dueDate  : theDueDate?.toDate()
        } else {
            alert.dueDate = isChangeDueDate ? theDueDate?.toDate() : alert.dueDate
            if(disposition && resetDate && (disposition.reviewCompleted || disposition.closed) && !dispReviewPeriod){
                alert.dueDate = null
            }
        }

        alert.dueDate
    }

    def static calcDueDateForSignalWorkflow(alert, priority, Disposition disposition = null, boolean resetDate = false, List dispositionConfigs=[],Date date = null) {
        // TODO change the following two lines
        if (!alert || !priority)
            return
        String serverTimeZone = Holders.config.server.timezone
        def wsUpdatedDate = new DateTime(alert.wsUpdated, DateTimeZone.forID(serverTimeZone))
        if(!wsUpdatedDate && date){
            wsUpdatedDate = new DateTime(date,DateTimeZone.forID(serverTimeZone))
        }
        Integer reviewPeriod = priority.reviewPeriod
        Integer dispReviewPeriod = null
        Boolean isChangeDueDate = true
        if(disposition){
            dispReviewPeriod = dispositionConfigs?.find {it -> it.disposition == disposition}?.reviewPeriod
            if(dispReviewPeriod) {
                reviewPeriod = dispReviewPeriod
            } else if(resetDate){
                isChangeDueDate = false
            }
        }
        def reviewDate = new DateTime(new Date(), DateTimeZone.forID(serverTimeZone))
        if(resetDate) {
            alert.reviewDate = reviewDate.toDate()
        }
        def theDueDate = resetDate ? reviewDate.plusDays(reviewPeriod) : wsUpdatedDate.plusDays(reviewPeriod)
        if (alert instanceof ValidatedSignal) {
            alert.dueDate = alert.isDueDateUpdated ? alert.dueDate  : theDueDate.toDate()
        } else {
            alert.dueDate = isChangeDueDate ? theDueDate.toDate() : alert.dueDate
        }
        if(disposition && resetDate && (disposition.reviewCompleted || disposition.closed) && !dispReviewPeriod){
            alert.dueDate = null
        }
        alert.dueDate
    }

    public static List<Map> getEventDictionaryValues(String usedEventSelection) {
        List<Map> result = [[:], [:], [:], [:], [:], [:], [:], [:]]
        parseDictionary(result, usedEventSelection)
        return result
    }

    public static List<Map> getProductDictionaryValues(String productSelection, Boolean isPva = true) {
        List<Map> result = isPva ? PVDictionaryConfig.ProductConfig.columns.collect { [:] } : [[:], [:], [:], [:]]
        parseDictionary(result, productSelection)
        return result
    }

    public static List<Map> getProductDictionaryValues(List<ProductGroup> productGroups, Boolean isPva = true) {
        List<Map> result = isPva ? PVDictionaryConfig.ProductConfig.columns.collect { [:] } : [[:], [:], [:], [:]]
        parseDictionary(result, productGroups)
        return result
    }

    public static List<Map> getStudyDictionaryValues(String studySelection, Boolean isPva = true) {
        List<Map> result = isPva ? PVDictionaryConfig.StudyConfig.columns.collect { [:] } : [[:], [:], [:]]
        parseDictionary(result, studySelection)
        return result
    }

    public static List<Map> getProductDrugValues(String productSelection, Boolean isPva = true) {
        isPva && productSelection ? parseDictionaryDrug(productSelection) : []
    }

    /**
     * Get list of drug record ids and name for a product selection
     * @param dictionarySelection
     * @return list of drug record ids and name
     */
    public static List<Map> parseDictionaryDrug(String dictionarySelection) {
        List<Map> result = []
        if(!dictionarySelection) {
            return result
        }
        Map values = new JsonSlurper().parseText(dictionarySelection)
        List drugMapList = values.get(Constants.DRUG_RECORD_NUMBER_LEVEL)
        drugMapList.each{
            Map map = [id: it["id"], name: it["name"]]
            result << map
        }
        result
    }

    public static parseDictionary(List<Map> result, String dictionarySelection) {
        if (dictionarySelection) {
            Map values = new JsonSlurper().parseText(dictionarySelection)
            values.each { k, v ->
                if (!k.equals("isMultiIngredient") && !k.equals(Constants.DRUG_RECORD_NUMBER_LEVEL)) { // isWhoDrug check required if its present in product selection
                    int level = k.toInteger()
                    if(level!=Constants.DRUG_RECORD_NUMBER_LEVEL_IDX){
                        v.each {
                            result[level - 1].put(it["id"], it["name"])
                        }
                    }
                }
            }
        }
    }

    public static parseDictionary(List<Map> result, List<ProductGroup> productGroups) {
        if (productGroups) {
            productGroups.each { productGroup ->
                parseDictionary(result, productGroup.productSelection)
            }
        }
    }

    def static getClass(Class objectClass, String propertyName) {

        def propertyClass = null
        try {
            propertyClass = objectClass.getDeclaredField(propertyName)?.type
        }
        catch (NoSuchFieldException ex){
            //no op
        }
        return propertyClass

    }

    public static boolean validateScheduleDateJSON(String scheduleDateJSON) {
        if (scheduleDateJSON.contains("FREQ=WEEKLY") && scheduleDateJSON.contains("BYDAY=;")) {
            return false
        }
        return true
    }

    public static String getValidFileName( String name, int fileLength = 230 ) {
        name = EmojiUtils.removeEmoji( name )
        Pattern regex = Pattern.compile( "[#%&{}\\ <> *?/\$!'\":;^|/.@+`|=]" )
        name = name.replaceAll( regex, " " )
        if( name.length() >= fileLength )
            name = name.substring( 0, fileLength )

        return name
    }

    public static def getBean(String name) {
        return Holders.applicationContext.getBean(name)
    }

    public static getPersistentProperties(theInstance) {
        def grailsDomainClassMappingContext = getBean("grailsDomainClassMappingContext")
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(theInstance.class.name)
        return entityClass.persistentProperties*.name
    }

    static String getCustomErrorMessage(grails.validation.ValidationException vx) {
        List<ObjectError> allErrors = vx.errors.getAllErrors()
        String errorMessage = "<div role=\"alert\" style=\"word-break: break-all\"> <button type=\"button\" class=\"close\" data-dismiss=\"alert\">\n" +
                "            <span  onclick=\"this.parentNode.parentNode.remove(); return false;\">x</span>\n" +
                "            <span class=\"sr-only\"><g:message code=\"default.button.close.label\" /></span>\n" +
                "        </button><ul>"
        boolean added=false
        allErrors?.each {
            if( it?.arguments?.length >= 4 ) {
                errorMessage = errorMessage + "<li>" + it?.arguments[ 3 ] + "</li>"
            }
        }
        errorMessage + "</ul></div>"
        if (!allErrors) {
            errorMessage = ""
        }
        return errorMessage
    }
    static List<String> getCustomErrorMessageList(grails.validation.ValidationException vx) {
        List<ObjectError> allErrors = vx?.errors?.getAllErrors()
        List<String> allErrorsList=[]
        boolean added=false
        allErrors?.each {
            if( it?.arguments?.length >= 4 ) {
                allErrorsList?.add( it?.arguments[ 3 ] )
            }
        }
        return allErrorsList
    }
    static def validator(String value, String obj, String[] chars) {
        if (!chars) {
            chars = Constants.SpecialCharacters.DEFAULT_CHARS;
        }
        def invalidSpecialCharacters = value?.findAll { a ->
            chars?.any { a.contains(it) }
        }?.unique()
        if (invalidSpecialCharacters) {
            return ['validation.special.character.error', String.join(" , ", invalidSpecialCharacters) + " special character" + (invalidSpecialCharacters?.size() > 1 ? "s are" : " is") + " not allowed in " + obj]
        } else {
            return true
        }
    }

    public static def getReadableStartDateTime(String startDateTimeStr){
        startDateTimeStr = startDateTimeStr.replaceAll("-\\d{2}:\\d{2}", "")
        Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.SCHEDULE_DATE)
        Date date = (Date) dateFormatter.parseObject(startDateTimeStr)
        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATETIME)
        return dateFormat.format(date)
    }

    public static getDashboardVisibleWidgets(String jsonData, Boolean isHealthChecker) {
        if (jsonData == "" || jsonData == null || jsonData == "null") {
            return ""
        }
        def slurper = new JsonSlurper()
        def parsedData = slurper.parseText(jsonData)

// Fetching reportWidgetName and visible values for entries where visible is true
        def widgetInfoList = parsedData.findAll { key, value ->
            if(!isHealthChecker && key=="pvWidgetChart-13")
            {
                return false
            }
            value instanceof Map && value.visible == true
        }.collect { key, value ->
            value.reportWidgetName
        }
        return widgetInfoList.sort()
    }

    /**
     * Sanitizes the signal name to generate a valid SpotFire file name.
     *
     * <p>
     * This method applies a regex pattern that is common for all other places where SpotFire files are used.
     * It also enforces a length limit for the file name as specified in PVS-68227.
     * </p>
     *
     * @param signalName The original name of the signal which needs to be sanitized.
     * @param spotFireFileNameLength The length limit for the file name.
     * @return A valid and sanitized file name with the specified length limit.
     */
    static String generateSpotFireFileName(String signalName, int spotFireFileNameLength) {
        String fileName = signalName
                ?.replaceAll(Constants.DOUBLE_BACKSLASH_NON_ALPHANUMERIC_SPOTFIRE_SPECIFIC_REGEX, Constants.Commons.UNDERSCORE)
                ?.replaceAll(Constants.LEADING_NONWORD_UNDERSCORE_REGEX, Constants.Commons.S_UNDERSCORE)

        fileName = getValidFileName(fileName, spotFireFileNameLength)
        return fileName
    }


    static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    static String sanitizeString(String input) {
        if (input == null || input.isEmpty()) {
            return ""; // Handle null or empty input
        }

        // Remove the specific special characters: <, >, !, |, /, \, #
        String sanitized = input.replaceAll("[<>!|/#\\\\]", "");

        // Restrict the length to 100 characters
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized;
    }

}
