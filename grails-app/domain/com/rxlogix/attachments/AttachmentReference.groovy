package com.rxlogix.attachments

import com.rxlogix.Constants
import com.rxlogix.enums.AttachmentType
import com.rxlogix.signal.References
import com.rxlogix.util.AttachmentableUtil
import grails.plugins.orm.auditable.ChildModuleAudit
import grails.util.Holders

@ChildModuleAudit(parentClassName = ['references'])
class AttachmentReference {
    static auditable = [ignore:['length','posterClass','ext','posterId','contentType','savedName','isDeleted','dateCreated','lastUpdated','referenceType','referenceLink']]

    // file
    String name
    String ext
    String contentType
    Long length
    Date dateCreated
    Date lastUpdated

    // poster
    String posterClass
    Long posterId

    // input name
    String inputName
    String referenceLink
    String savedName
    AttachmentType attachmentType = AttachmentType.Attachment
    String referenceType

   // static belongsTo = [lnk: AttachmentLink]

    static constraints = {
        name nullable: false, blank: false
        ext nullable: true, blank: true
        contentType nullable: true, blank: true
        length min: 0L

        posterClass blank: false, nullable: true
        posterId min: 0L
        attachmentType nullable: false
        referenceLink nullable: true, maxSize: 4000
        savedName nullable: true
        referenceType nullable: true
    }
    static transients = ['filename', 'path', 'niceLength', 'poster']
    static searchable = {
        only = ['name', 'ext', 'path']
        path converter: Holders.config.grails.attachmentable.searchableFileConverter ?: 'string'
    }

    static mapping = {
        table("ATTACHMENT_REFERENCE")
        name sqlType: "varchar2(4000 CHAR)"
        inputName sqlType: "varchar2(4000 CHAR)"
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
        return References.findByAttachment(this)?.getInstanceIdentifierForAuditLog() ?: inputName
    }

}
