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

import com.rxlogix.attachments.exceptions.AttachmentableException
import com.rxlogix.attachments.exceptions.EmptyFileException
import com.rxlogix.enums.AttachmentType
import com.rxlogix.signal.AttachmentDescription
import com.rxlogix.signal.SignalEmailLog
import com.rxlogix.user.User
import com.rxlogix.util.AttachmentableUtil
import grails.orm.PagedResultList
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.apache.tika.mime.MimeTypes
import com.rxlogix.exception.FileFormatException
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.apache.tika.Tika

import org.apache.tika.mime.MimeType
import java.lang.reflect.UndeclaredThrowableException

class AttachmentableService {
    static transactional = false

    def grailsApplication

    /* -------------------------- ATTACHMENT LINK --------------------------- */

    AttachmentLink getAttachmentLink(Long attachmentId) {
        def link = AttachmentLink.withCriteria(uniqueResult: true) {
            attachments {
                idEq attachmentId
            }
        }
        link
    }

    /* ------------------------------- POSTER ------------------------------- */

    def getPoster(Attachment attachment) {
      attachment.poster
    }

    /* ----------------------------- ATTACHMENT ----------------------------- */

    // add

    /**
     * Upload a list of files.
     * @param User poster
     * @param reference
     * @param List<MultipartFile> files
     * @return a list of successfully uploaded files
     */
    List<MultipartFile> upload(User poster,
                               def reference,
                               List<MultipartFile> files,String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null, Boolean updateRmms = false) {
        def uploadedFiles = []

        try {
            Attachment.withTransaction {status ->
                Integer filesCount = files.size()
                files.each {MultipartFile file ->
                    try {
                        if(file?.originalFilename) {
                            checkExtension(file.originalFilename)
                            addAttachment(poster, reference, file, inputName, attachmentType, fileName, rmmFileName, updateRmms)
                            uploadedFiles << file
                        }
                    } catch (Exception e) {
                        if (e instanceof EmptyFileException) {
                            log.error "Error adding attachment: ${e.message}"
                        } else if (e instanceof UndeclaredThrowableException
                                && e.cause instanceof EmptyFileException) {
                            log.error "Error adding attachment: ${e.cause.message}"
                        } else if(e instanceof FileFormatException) {
                            if(filesCount == 1) {
                                throw e
                            }
                        } else {
                            status.setRollbackOnly()
                            log.error "Error adding attachment", e
                        }
                    }
                }
            }
        } catch(FileFormatException exception) {
            throw exception
        }
        catch (Exception e) {
            log.error "Error adding attachment: ${e.message}"
        }

        uploadedFiles
    }

    def addAttachment(def poster, def reference, CommonsMultipartFile file,String inputName=null) {
        doAddAttachment(grailsApplication.config, poster, reference, file,inputName)
    }

    def addAttachment(User poster, def reference, MultipartFile file,String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null, Boolean updateRmms = false) {
        doAddAttachment(grailsApplication.config, poster, reference, file,inputName,attachmentType,fileName,rmmFileName,null,updateRmms)
    }

    private Object doAddAttachment(def config,
                      User poster,
                      def reference,
                      MultipartFile file,String inputName=null,String attachmentType=null,String inputFileName= null,String rmmFileName = null, def ext = null, Boolean updateRmms = false) {
        if (reference.ident() == null) {
            throw new AttachmentableException(
                "You must save the entity [${delegate}] before calling addAttachment.")
        }

        if (!file?.size) {
            throw new EmptyFileException(file.name, file.originalFilename)
        }

        String delegateClassName = AttachmentableUtil.fixClassName(reference.class)
        String posterClass = (poster instanceof String) ? poster : AttachmentableUtil.fixClassName(poster.class.name)
        Long posterId = (poster instanceof String) ? 0L : poster.id
        String filename = file.originalFilename

        // link
        AttachmentLink link = AttachmentLink.findByReferenceClassAndReferenceId(
                delegateClassName, reference.ident())
        if (!link) {
            link = new AttachmentLink(
                    referenceClass: delegateClassName,
                    referenceId: reference.ident())
        }

        String savedName = getRandomNameFile()
        def params = RequestContextHolder?.requestAttributes?.params

        // attachment
        List<Attachment> attachmentObjList = reference.getAttachments()?.sort { it.dateCreated }
        Attachment attachment = null

        if (params?.attachmentId != null && params?.attachmentId != "") {
            //this is to prevent new file creatn in file update for signal refernce
            attachment = Attachment.get(params.attachmentId as Long)
        }
        Boolean isEmailLog = SignalEmailLog.class.equals(reference?.class)

        if (!isEmailLog && ((updateRmms && link?.attachments?.size() > 0) || attachment != null)) {
            Attachment attachmentObj = attachment ? attachment : link?.attachments[link?.attachments?.size() - 1]
            attachmentObj.setProperties([
                    name          : FilenameUtils.getBaseName(filename),
                    ext           : FilenameUtils.getExtension(filename),
                    contentType   : file.contentType,
                    // poster
                    posterClass   : posterClass ?: "",
                    posterId      : posterId,
                    referenceLink : null,
                    attachmentType: AttachmentType.Attachment
            ]) as Attachment
            attachment = attachmentObj
        } else{
            attachment = new Attachment(
                    // file
                    name: FilenameUtils.getBaseName(filename),
                    ext: FilenameUtils.getExtension(filename),
                    length: 0L,
                    contentType: file.contentType,
                    // poster
                    posterClass: posterClass ?: "",
                    posterId: posterId,
                    attachmentType: AttachmentType.Attachment
                    // input
            )
        }
        if (isEmailLog) {
            attachment.skipAudit = true
        }
        if(inputFileName){
            attachment.ext =inputFileName.substring(inputFileName.indexOf(".")+1)
            attachment.savedName = inputFileName
        } else {
            attachment.savedName =savedName
            attachment.ext =FilenameUtils.getExtension(filename)
        }
        if(attachmentType){
            attachment.referenceType=attachmentType
        }
        if(inputFileName || inputName) {
            if (inputName) {
                attachment.inputName = inputName
            } else if (inputFileName) {
                attachment.inputName = inputFileName.substring(0, inputFileName.indexOf("."))
            } else {
                attachment.inputName = FilenameUtils.getBaseName(filename)
            }
        } else {
            if (rmmFileName) {
                if (params?.action != null && params?.action != "" && params.action == "sendEmailForRmms") {
                    attachment.name = rmmFileName
                }
                attachment.inputName = rmmFileName
            } else {
                attachment.inputName = filename
            }
        }
        if(!attachment.ext){
            attachment.ext = ext
        }
        if (null != params?.description && params?.description != "" && params?.isAlertDomain == true) {
           // TODO : Need to change this but as of now this is done for specfic scenarios mentioned in ticket PVS-49054
            attachment?.auditDescription = params?.description
        }
        link.addToAttachments attachment

        File diskFile = AttachmentableUtil.getFile(config, attachment, true)
        file.transferTo(diskFile)
        checkType(diskFile)

        if (!link.save(flush: true)) {
            throw new AttachmentableException(
                    "Cannot create Attachment for arguments [$poster, $file], they are invalid.")
        }

        // save file to disk
        attachment.length = diskFile.length()

        // interceptors
        if(reference.respondsTo('onAddAttachment')) {
            reference.onAddAttachment(attachment)
        }

        attachment.save(flush:true) // Force update so searchable can try to index it again.

        return reference
    }
    Object uploadAssessmentReport(User poster, def reference, def file,String fileName) {
        def config=grailsApplication.config
         Attachment.withTransaction {status ->
                 String delegateClassName = AttachmentableUtil.fixClassName(reference.class)
                 String posterClass = (poster instanceof String) ? poster : AttachmentableUtil.fixClassName(poster.class.name)
                 Long posterId = (poster instanceof String) ? 0L : poster.id
                 // link
                 AttachmentLink link = new AttachmentLink(
                             referenceClass: delegateClassName,
                             referenceId: reference.ident())

                 // attachment
                 Attachment attachment = new Attachment(
                         // file
                         name: fileName,
                         savedName: fileName + '.PDF',
                         ext: 'PDF',
                         length: 0L,
                         contentType: 'application/pdf',
                         // poster
                         posterClass: posterClass ?: "",
                         posterId: posterId,
                         // input
                         inputName: fileName
                 )

             attachment.referenceType= 'Others'
             link.addToAttachments attachment

                 File diskFile = AttachmentableUtil.getFile(config, attachment, true)
                 file.renameTo(diskFile)
                 checkType(diskFile)

                 if (!link.save(flush: true)) {
                     throw new AttachmentableException(
                             "Cannot create Attachment for arguments [$poster, $file], they are invalid.")
                 }

                 // save file to disk
                 attachment.length = diskFile.length()

                 // interceptors
                 if (reference.respondsTo('onAddAttachment')) {
                     reference.onAddAttachment(attachment)
                 }

                 attachment.save(flush: true) // Force update so searchable can try to index it again.
             }
        return reference
    }

//    This method is used in topic migration to validated signal.
    Attachment doAddAttachmentForTopicMigration(def poster, def reference, def file) {
        def config = grailsApplication.config
        if (reference.ident() == null) {
            throw new AttachmentableException("You must save the entity [${delegate}] before calling addAttachment.")
        }

        if (!file?.size) {
            throw new EmptyFileException(file?.name, file?.originalFilename)
        }

        String delegateClassName = AttachmentableUtil.fixClassName(reference.class)
        String posterClass = (poster instanceof String) ? poster : AttachmentableUtil.fixClassName(poster.class.name)
        Long posterId = (poster instanceof String) ? 0L : poster.id
        String filename = file.originalFilename

        // link
        def link = AttachmentLink.findByReferenceClassAndReferenceId(
                delegateClassName, reference.ident())
        if (!link) {
            link = new AttachmentLink(
                    referenceClass: delegateClassName,
                    referenceId: reference.ident())
        }

        // attachment
        Attachment attachment = new Attachment(
                // file
                name: FilenameUtils.getBaseName(filename),
                ext: FilenameUtils.getExtension(filename),
                length: 0L,
                contentType: file.contentType,
                // poster
                posterClass: posterClass ?: "",
                posterId: posterId,
                // input
                inputName: file.name)
        link.addToAttachments attachment

        if (!link.save(flush: true)) {
            throw new AttachmentableException("Cannot create Attachment for arguments [$poster, $file], they are invalid.")
        }

        // save file to disk
        File diskFile = AttachmentableUtil.getFile(config, attachment, true)
        file.transferTo(diskFile)

        attachment.length = diskFile.length()

        // interceptors
        if (reference.respondsTo('onAddAttachment')) {
            reference.onAddAttachment(attachment)
        }

        attachment.save(flush: true) // Force update so searchable can try to index it again.

        return attachment
    }

    // remove

    int removeAttachments(def reference) {
        def cnt = 0
        def dir = AttachmentableUtil.getDir(grailsApplication.config, reference)
        def files = []
        reference.getAttachments()?.collect {
            files << AttachmentableUtil.getFile(grailsApplication.config, it)
        }

        def lnk = AttachmentLink.findByReferenceClassAndReferenceId(
                reference.class.name, reference.ident())
        if (lnk) {
            try {
                lnk.delete(flush: true)
                files.each {File file ->
                    cnt++
                    AttachmentableUtil.delete(file)
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error "Error deleting attachments: ${e.message}"
            }

            if (cnt) {
                AttachmentableUtil.delete(dir)
            }
        }

        cnt
    }

    boolean removeAttachment(Long attachmentId) {
        removeAttachment(Attachment.get(attachmentId))
    }

    boolean removeAttachment(Attachment attachment) {
        try {
            if (attachment.attachmentType == AttachmentType.Attachment) {
                File file = AttachmentableUtil.getFile(grailsApplication.config, attachment)
                AttachmentableUtil.delete(file)
            }

            AttachmentDescription ad = AttachmentDescription.findByAttachment(attachment)
            attachment.auditDescription = ad?.description// Added for addition of description in deleted audit of attachments PVS-54049
            if (ad)
                ad.delete()
            AttachmentLink lnk = attachment.lnk
            attachment.delete(flush: true)
            removeUnusedLinks()
            return true
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error "Error deleting ${attachment.attachmentType == AttachmentType.Attachment ? 'attachment' : 'reference'}: ${e.message}"
        }

        false
    }

    int removeAttachments(def reference, List inputNames) {
        def cnt = 0
        def attachments = AttachmentLink.executeQuery("""
            select a
                Attachment a inner join a.lnk link
            where
                a.inputName in (:inputNames)
                and
                link.referenceClass = :referenceClass
                and
                link.referenceId = :referenceId""",
            [referenceClass: reference.class.name, referenceId: reference.ident(),
                    inputNames: inputNames])

        attachments?.each {Attachment attachment ->
            File file = AttachmentableUtil.getFile(grailsApplication.config, attachment)

            try {
                attachment.delete(flush: true)
                cnt++
                AttachmentableUtil.delete(file)
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error "Error deleting attachments: ${e.message}"
            }
        }

        removeUnusedLinks()

        cnt
    }

    private int removeUnusedLinks() {
        int result = Attachment.executeUpdate(
                'delete from AttachmentLink link where link.attachments is empty')
        result
    }

    // count

    int countAttachmentsByReference(def reference, List inputNames = []) {
        if (!reference) {
            throw new AttachmentableException(
                    "Reference is null.")
        }

        if (!reference.ident()) {
            throw new AttachmentableException(
                    "Reference [$reference] is not a persisted instance.")
        }

        int result = Attachment.createCriteria().get {
            projections {
                rowCount()
            }
            if (inputNames) {
                inList 'inputName', inputNames
            }
            lnk {
                eq 'referenceClass', reference.class.name
                eq 'referenceId', reference.ident()
            }
            cache true
        }
        result
    }

    int countAttachmentsByPoster(def poster) {
        if (!poster) {
            throw new AttachmentableException("Poster is null.")
        }

        if (! (poster instanceof String) && !poster.id) {
            throw new AttachmentableException(
                    "Poster [$poster] is not a persisted instance.")
        }

        int result = Attachment.createCriteria().get {
            projections {
                rowCount()
            }
            eq "posterClass", (poster instanceof String) ? poster : poster.class.name
            eq 'posterId', (poster instanceof String) ? 0L : poster.id
            cache true
        }

        result
    }

    // find

    PagedResultList findAttachmentsByPoster(def poster, def params = [:]) {
        if (!poster) {
            throw new AttachmentableException("Poster is null.")
        }

        if (! (poster instanceof String) && !poster.id) {
            throw new AttachmentableException(
                    "Poster [$poster] is not a persisted instance.")
        }

        params.order = params.order ?: 'desc'
        params.sort = params.sort ?: 'dateCreated'
        params.cache = true

        PagedResultList result = Attachment.createCriteria().list(params) {
            eq "posterClass", (poster instanceof String) ? poster : poster.class.name
            eq 'posterId', (poster instanceof String) ? 0L : poster.id
        }

        result
    }

    PagedResultList findAttachmentsByReference(def reference, List inputs = [], Map params = [:]) {
        if (!reference) {
            throw new AttachmentableException(
                    "Reference is null.")
        }

        if (!reference.ident()) {
            throw new AttachmentableException(
                    "Reference [$reference] is not a persisted instance.")
        }

        params.order = params.order ?: 'desc'
        params.sort = params.sort ?: 'dateCreated'
        params.cache = true

        PagedResultList result = Attachment.createCriteria().list(params) {
            if (inputs) {
                inList 'inputName', inputs
            }
            eq 'attachmentType', AttachmentType.Attachment
            lnk {
                eq 'referenceClass', reference.class.name
                eq 'referenceId', reference.ident()
            }
        }

        result
    }

    PagedResultList findReferenceLinkByReference(def reference, List referenceLinks = [], Map params = [:]) {
        if (!reference) {
            throw new AttachmentableException(
                    "Reference is null.")
        }

        if (!reference.ident()) {
            throw new AttachmentableException(
                    "Reference [$reference] is not a persisted instance.")
        }

        params.order = params.order ?: 'desc'
        params.sort = params.sort ?: 'dateCreated'
        params.cache = true

        PagedResultList result = Attachment.createCriteria().list(params) {
            if (referenceLinks) {
                inList 'referenceLink', referenceLinks
            }
            eq 'attachmentType', AttachmentType.Reference
            lnk {
                eq 'referenceClass', reference.class.name
                eq 'referenceId', reference.ident()
            }
        }

        result
    }

    def attachUploadFileTo(User user, List inputNames, reference, MultipartHttpServletRequest request, String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null, Boolean updateRmms = false) {
        // files
        List<MultipartFile> filesToUpload = []
        List<MultipartFile> uploadedFiles = []

        if (request instanceof MultipartHttpServletRequest) {
            request.multipartFiles.each {k, v ->
                if (v instanceof List) {
                    v.each {MultipartFile file ->
                        filesToUpload << file
                    }
                } else {
                    filesToUpload << v
                }
            }

            // upload
            uploadedFiles = upload(user, reference, filesToUpload,inputName,attachmentType,fileName,rmmFileName, updateRmms)
        }

        [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles]
    }
    def attachUploadFileToRMMs(User user, reference, List<MultipartFile> filesToUpload, String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null, Boolean updateRmms = false) {
        List<MultipartFile> uploadedFiles = []
        uploadedFiles = upload(user, reference, filesToUpload,inputName,attachmentType,fileName,rmmFileName, updateRmms)
        [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles]
    }

    Object doAddReference(User poster, def reference, String referenceUrl,String inputName = null,String attachmentType=null, Boolean updateRmms = false) {
        if (reference.ident() == null) {
            throw new AttachmentableException(
                    "You must save the entity [${delegate}] before calling addAttachment.")
        }

        String delegateClassName = AttachmentableUtil.fixClassName(reference.class)
        String posterClass = (poster instanceof String) ? poster : AttachmentableUtil.fixClassName(poster.class.name)
        Long posterId = (poster instanceof String) ? 0L : poster.id
        // link
        AttachmentLink link = AttachmentLink.findByReferenceClassAndReferenceId(
                delegateClassName, reference.ident())
        if (!link) {
            link = new AttachmentLink(
                    referenceClass: delegateClassName,
                    referenceId: reference.ident())
        }
        if(inputName){
            inputName = inputName
        } else {
            inputName = referenceUrl
        }
        // attachment

        def params = RequestContextHolder?.requestAttributes?.params
        Attachment attachment=null

        if (params.attachmentId != null && params.attachmentId != "") {
            //this is to prevent new file creatn in file update for signal refernce
            attachment = Attachment.get(params.attachmentId as Long)
        }
        if(updateRmms && link?.attachments?.size()>0 || attachment != null){
            Attachment attachmentObj = link.attachments[link.attachments.size() - 1]
            attachmentObj.setProperties([
                    name: referenceUrl,
                    length: 0L,
                    // poster
                    posterClass: posterClass ?: "",
                    posterId: posterId,
                    // input
                    referenceLink: referenceUrl,
                    ext: null,
                    contentType: null,
                    savedName:null,
                    attachmentType: AttachmentType.Reference
            ]) as Attachment
            attachment = attachmentObj
        }else {
            attachment = new Attachment(
                    // file
                    name: referenceUrl,
                    length: 0L,
                    // poster
                    posterClass: posterClass ?: "",
                    posterId: posterId,
                    // input
                    referenceLink: referenceUrl,
                    attachmentType: AttachmentType.Reference)
        }
        if(attachmentType){
            attachment.referenceType=attachmentType
        }
        if(inputName){
            attachment.inputName=inputName
        } else {
            attachment.inputName=referenceUrl
        }
        link.addToAttachments(attachment)
        if (!link.save(flush: true)) {
            throw new AttachmentableException(
                    "Cannot create Reference Link for arguments [$poster, $referenceUrl], they are invalid.")
        }

        attachment.save(flush: true, failonError: true) // Force update so searchable can try to index it again.

        return reference
    }

    String getRandomNameFile() {
        String state = UUID.randomUUID().toString()
        return state
    }

    void checkExtension(String fileName) {
        List formatsNotSupported = Holders.config.signal.file.not.accepted.formats
        List formatsSupported = Holders.config.signal.file.accepted.formats
        formatsSupported = formatsSupported.minus(formatsNotSupported)
        if(!formatsSupported.contains('.' + FilenameUtils.getExtension(fileName)?.toLowerCase())) {
            throw new FileFormatException("File Format is not correct")
        }
    }

    void checkType(File file) {
        Tika tika = new Tika()
        String type = tika.detect(file)
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes()
        MimeType mimeType = allTypes.forName(type)
        if(type.contains('javascript') || type.contains('html')) {
            throw new FileFormatException("HTML and JS files not allowed")
        }

    }
}
