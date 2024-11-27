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

import com.rxlogix.SignalAuditLogService
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.util.AttachmentableUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.apache.commons.io.FilenameUtils

import javax.servlet.http.HttpServletResponse

@Secured(["isAuthenticated()"])
class AttachmentableController {
    def attachmentableService
    def activityService
    def userService
    SignalAuditLogService signalAuditLogService
    // download

    def download() {
        Attachment attachment = Attachment.get(params.id as Long)

        if (attachment) {
            File file = AttachmentableUtil.getFile(grailsApplication.config, attachment)

            if (file.exists()) {
                String filename
                if(params.type == "assessment" || params.type == "rmm"){
                    filename = attachment.filename
                } else {
                    String extension = FilenameUtils.getExtension(attachment.filename)
                    if(attachment.inputName && !attachment.inputName.contains(extension))
                        filename = attachment.inputName + "." + extension ?: attachment.filename
                    else
                        filename = attachment.inputName ?: attachment.filename
                }

                ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$filename\"",
                    'Cache-Control': 'private',
                    'Pragma': ''].each {k, v ->
                    response.setHeader(k, v)
                }

                if (params.containsKey('withContentType')) {
                    response.contentType = attachment.contentType
                } else {
                    response.contentType = 'application/octet-stream'
                }
                file.withInputStream{fis->
                    response.outputStream << fis
                }
                signalAuditLogService.createAuditForExport(null, attachment.getInstanceIdentifierForAuditLog(),attachment.getModuleNameForMultiUseDomains(), [:], filename)
                // response.contentLength = file.length()
                // response.outputStream << file.readBytes()
                // response.outputStream.flush()
                // response.outputStream.close()
                return
            }
        }

        response.status = HttpServletResponse.SC_NOT_FOUND
    }

    def show() {
        // Default show action is to display the attachment inline in the browser.
        if (!params.containsKey('inline')) {
            params.inline = ''
        }
        if (!params.containsKey('withContentType')) {
            params.withContentType = ''
        }
        forward(action:'download', params:params)
    }

    // upload

    def upload() {
        AttachmentLink lnk = new AttachmentLink(params.attachmentLink)

        attachUploadedFilesTo(lnk.reference)

        render 'success'
    }

    def uploadInfo() {
        uploadStatus()
    }

    // delete
    def delete(Long id, Long alertId) {
        Attachment attachment = Attachment.get(id)
        String fileName = attachment.getFilename()
        def result = attachmentableService.removeAttachment(id)
        activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                userService.getUser(), "Attachment [" + fileName + "] is removed", null)

        if (params.returnPageURI) {
            redirect url: params.returnPageURI - request.contextPath
        } else {
            render (result > 0 ? 'success' : 'failed')
        }
    }

    def fetchAcceptedFileFormats() {
        List formatsSupported = Holders.config.signal.file.accepted.formats
        List formatsNotSupported = Holders.config.signal.file.not.accepted.formats
        formatsSupported = formatsSupported.minus(formatsNotSupported)
        render formatsSupported as JSON
    }

}
