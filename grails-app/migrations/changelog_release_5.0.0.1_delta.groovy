import com.rxlogix.Constants
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "sandeep (generated)", id: "1608824569972-101") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(*) from ATTACHMENT;")
            }
        }
        sql("""UPDATE ATTACHMENT SET REFERENCE_TYPE='Others' where REFERENCE_TYPE is null and ATTACHMENT_TYPE = 'Attachment' AND LNK_ID in( SELECT ID from ATTACHMENT_LINK where REFERENCE_CLASS = 'com.rxlogix.signal.ValidatedSignal');
                     UPDATE ATTACHMENT SET REFERENCE_TYPE='Reference' where REFERENCE_TYPE is null and ATTACHMENT_TYPE = 'Reference' AND LNK_ID in( SELECT ID from ATTACHMENT_LINK where REFERENCE_CLASS = 'com.rxlogix.signal.ValidatedSignal');
                     COMMIT;
                     """)
    }

    changeSet(author: "sandeep (generated)", id: "1608824569982-102") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'COMMENT_BACKUP')
        }

        def config = Holders.grailsApplication.config
        String nextLine = Constants.Alias.NEXT_LINE
        String descriptionStartComment = config.signal.description.migration.start.comment + nextLine
        String descriptionEndComment = nextLine + config.signal.description.migration.end.comment + nextLine

        sql("""update validated_signal set generic_comment = '${descriptionStartComment}' || description ||  '${descriptionEndComment}' || comment_backup;
                     alter table validated_signal
                     drop column comment_backup;
                     commit;
                  """)
    }

    changeSet(author: "sandeep (generated)", id: "1608824569982-103") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ATTACHMENT')
        }
        sql("""update attachment set saved_name = name || '.' || ext where saved_name is null and attachment_type = 'Attachment' and ext is not null;
                     update attachment set input_name = name where input_name = 'attachments';
                     commit;
                  """)
    }
}
