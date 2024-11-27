import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.util.AttachmentableUtil
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Nikhil (generated)", id: "18000217341123-1") {
        grailsChange {
            change {
                println "#######Legacy Attachment Migration Started######"
                try {
                    List referenceClassList = ['com.rxlogix.signal.ArchivedSingleCaseAlert', 'com.rxlogix.signal.ArchivedAggregateCaseAlert', 'com.rxlogix.config.ArchivedEvdasAlert', 'com.rxlogix.config.ArchivedLiteratureAlert']
                    List<Attachment> attachmentList
                    attachmentList = Attachment.createCriteria().list() {
                        lnk {
                            'in'('referenceClass', referenceClassList)
                        }

                    }
                    attachmentList.each { attachment ->
                        String referenceClass = ''
                        String currentReferenceClass = attachment?.lnk?.referenceClass
                        switch (currentReferenceClass) {
                            case 'com.rxlogix.signal.ArchivedSingleCaseAlert': referenceClass = 'com.rxlogix.signal.SingleCaseAlert'
                                break;
                            case 'com.rxlogix.signal.ArchivedAggregateCaseAlert': referenceClass = 'com.rxlogix.signal.AggregateCaseAlert'
                                break;
                            case 'com.rxlogix.config.ArchivedEvdasAlert': referenceClass = 'com.rxlogix.config.EvdasAlert'
                                break;
                            case 'com.rxlogix.config.ArchivedLiteratureAlert': referenceClass = 'com.rxlogix.config.LiteratureAlert'
                                break;
                        }
                        File currentFileDir = AttachmentableUtil.getDir(config, referenceClass, attachment?.lnk?.referenceId)
                        String filename = "${attachment.savedName}"
                        File fileFrom = new File(currentFileDir, filename)
                        def newDestination = AttachmentableUtil.getDir(Holders.config, currentReferenceClass, attachment?.lnk?.referenceId, true)
                        File fileDestination = AttachmentableUtil.getFile(Holders.config, attachment)
                        Boolean fileExistsAtDestination = fileDestination.exists()
                        println("File '" + fileFrom + "' exists at destination '" + newDestination + "': " + fileExistsAtDestination)
                        if (!fileExistsAtDestination && fileFrom.exists()) {
                            try {
                                ctx.productAssignmentImportService.moveFile(fileFrom, newDestination as String)
                                println("File moved Successfully!")
                            } catch (FileNotFoundException ex) {
                                println('Error occurred while moving file')
                                println ex
                            }
                        } else {
                            println("Skipping this file!!!")
                        }
                    }
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while upgrading the signal. #############")
                } finally {
                    confirm "Legacy Attachment Migration Complete."
                }
            }
        }
    }
}