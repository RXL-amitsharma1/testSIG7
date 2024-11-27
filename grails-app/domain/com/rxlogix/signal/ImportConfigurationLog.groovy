package com.rxlogix.signal

import com.rxlogix.enums.ImportConfigurationProcessState
import com.rxlogix.user.User

class ImportConfigurationLog {

    static auditable = false

    String importFileName
    String importLogFileName
    String importConfType
    User importedBy
    Date importedDate
    ImportConfigurationProcessState status
    String nodeUuid

    static mapping = {
        table("IMPORT_CONFIGURATION_LOG")
    }

    static constraints = {
        importedDate nullable: true
        importLogFileName nullable: true
        importConfType: true
        nodeUuid nullable: true
    }
}
