/* Copyright 2010 Mihai Cazacu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rxlogix.attachments

import com.rxlogix.Constants
import com.rxlogix.enums.AttachmentType
import com.rxlogix.signal.AttachmentDescription
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.ChildModuleAudit
import grails.util.Holders
import org.springframework.web.context.request.RequestContextHolder
import com.rxlogix.signal.SignalRMMs

@ChildModuleAudit(parentClassName = ['signalRMMs','attachmentDescription'])
@DirtyCheck
class Attachment {
    static auditable = [ignore:['ext','lnk','attachmentType','savedName','length','posterClass','contentType','posterId','version', 'lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy','modifiedBy','referenceLink']]

    // file
    String name
    String ext
    String contentType
    Long length
    Date dateCreated

    // poster
    String posterClass
    Long posterId

    // input name
    String inputName
    String referenceLink
    String savedName
    AttachmentType attachmentType = AttachmentType.Attachment
    String referenceType
    String auditDescription
    Boolean skipAudit = false

    static belongsTo = [lnk: AttachmentLink]

    static constraints = {
        name nullable: false, blank: false, maxSize: 8000
        ext nullable: true, blank: true
        contentType nullable: true, blank: true
        length min: 0L

        posterClass blank: false, nullable: true
        posterId min: 0L
        attachmentType nullable: false
        referenceLink nullable: true, maxSize: 8000
        savedName nullable: true
        referenceType nullable: true
        inputName maxSize: 8000
    }
    static transients = ['filename', 'path', 'niceLength', 'poster','auditDescription','skipAudit']
    static searchable = {
        only = ['name', 'ext', 'path']
        path converter: Holders.config.grails.attachmentable.searchableFileConverter ?: 'string'
    }

    static mapping = {
        //Removed second level cache use as it is resulting in wrong results on multi node setup
        name sqlType: "varchar2(8000 CHAR)"
        inputName sqlType: "varchar2(8000 CHAR)"
    }

    String toString() {
        filename
    }

    /* ------------------------------- UTILS -------------------------------- */

    String getFilename() {
        (attachmentType == AttachmentType.Attachment) && ext ? "$name.$ext" : "${getName()}"
    }

    String getName() {
        name
    }

    String getNiceLength() {
        if(length >= 10485760) { // 10 MB
            return "${(length / 1048576).intValue()} MB"
        } else if(length >= 1024) { // 1 kB
            return "${(length / 1024).intValue()} kB"
        }
        "$length"
    }

    String getPath() {
        AttachmentableUtil.getFile(Holders.config, this).absolutePath
    }

    def getPoster() {
        posterId == 0L ? posterClass : getClass().classLoader.loadClass(posterClass).get(posterId)
    }

    def getInstanceIdentifierForAuditLog() {
        try{
            String idForLink = name
            if(this.lnk != null){
                idForLink = Class.forName(this?.lnk?.referenceClass).get(this?.lnk?.referenceId)?.getInstanceIdentifierForAuditLog() ?: name
            }
            return idForLink
        }catch(Exception ex){
            ex.getMessage()
            return name
        }

    }

    def getModuleNameForMultiUseDomains() {
        Map map = ["com.rxlogix.signal.ValidatedSignal"           : "Signal: Reference",
                   "com.rxlogix.signal.ArchivedAggregateCaseAlert": "Aggregate Review: Archived Alert: Attachment",
                   "com.rxlogix.signal.SignalRMMs"                : SignalRMMs.findById(this?.lnk?.referenceId)?.getModuleNameForMultiUseDomains() ?: "Signal: RMMs",
                   "com.rxlogix.signal.SignalEmailLog"            : "Signal Email log",
                   "com.rxlogix.signal.AggregateCaseAlert"        : "Aggregate Review: Attachment",
                   "com.rxlogix.signal.ArchivedSingleCaseAlert"   : "Individual Case Review: Archived Alert: Attachment",
                   "com.rxlogix.config.EvdasAlert"                : "Evdas Review: Attachment",
                   "com.rxlogix.signal.AdHocAlert"                : "Ad-Hoc Review: Attachment",
                   "com.rxlogix.config.LiteratureAlert"           : "Literature Review: Attachment",
                   "com.rxlogix.config.ArchivedEvdasAlert"        : "EVDAS Review: Archived Alert: Attachment",
                   "com.rxlogix.signal.SingleCaseAlert"           : "Individual Case Review: Attachment",
                   "com.rxlogix.signal.Meeting"                   : "Signal: Meeting"
        ]
        return map.getOrDefault(this.lnk?.referenceClass, "Attachment")
    }

    def getEntityValueForDeletion(){
        AttachmentDescription ad = AttachmentDescription.findByAttachment(this)
        String description = ad?.description ?: (this.auditDescription != null ? this.auditDescription : "")

        if(this.lnk?.referenceClass == "com.rxlogix.signal.ValidatedSignal"){
            if(referenceLink != null){
                return "${this.getInstanceIdentifierForAuditLog()}: Reference Type-${AuditLogConfigUtil.splitCamelCase(referenceType)}, Description-${description}, File Name-${inputName}, Reference link-${referenceLink},  Created Date-${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
            }else{
                return "${this.getInstanceIdentifierForAuditLog()}: Reference Type-${AuditLogConfigUtil.splitCamelCase(referenceType)}, Description-${description}, File Name-${inputName}, Attach File-${savedName},  Created Date- ${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
            }
        }else{
            return "${this.getInstanceIdentifierForAuditLog()}: File Name-${inputName}, Description-${description}, Date Created- ${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
        }
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        def params = RequestContextHolder?.requestAttributes?.params
        if (this.auditDescription != null && this.auditDescription != "") {
            newValues.put("auditDescription", this.auditDescription)
        }

        if (params?.isAlertDomain && params.isAlertDomain == true) {
            //this is done to remove extra entry of file name (required for signal and rmms module) in PVS-57090 unless of writing custom ignore properties for domain checks
            if (newValues?.containsKey('name')) {
                newValues.remove('name')
            }
            if (oldValues?.containsKey('name')) {
                oldValues.remove('name')
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }
}
