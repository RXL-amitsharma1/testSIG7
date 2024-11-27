package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentReference
import com.rxlogix.attachments.exceptions.EmptyFileException
import com.rxlogix.enums.AttachmentType
import com.rxlogix.signal.References
import com.rxlogix.signal.UserReferencesMapping
import com.rxlogix.user.User
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.apache.tika.Tika
import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import com.rxlogix.exception.FileFormatException
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.transform.Transformers
import org.hibernate.type.BooleanType
import org.hibernate.type.IntegerType
import org.hibernate.type.LongType
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import javax.servlet.http.HttpServletRequest
import java.lang.reflect.UndeclaredThrowableException

@Transactional
class ReferencesService {
    def userService
    def grailsApplication
    def CRUDService
    def dataSource
    def sessionFactory
    def alertService


    def serviceMethod() {

    }

    Boolean checkIfFavIconExists(String url) {
        def code = new URL(url)
        try {
            def connection = code.openConnection()
            if (connection.responseCode == 200) {
                return true
            }
        } catch (Exception ex) {
            log.info("Error saving favIconUrl with exception" + ex)
        }
        return false
    }

    String getFavIconUrl(String url) {
        if (url) {
            if (!url.contains("https://") && !url.contains("http://")) {
                if (!url.contains("www.")) {
                    url = "www." + url
                }
                url = "https://" + url
                if (!url.contains(".com/")) {
                    if (url.endsWith(".com")) {
                        url = url + "/"
                    } else {
                        url = url + ".com/"
                    }
                }
            }
            Integer index = url.indexOf('/', 9)
            if (index != -1) {
                String favIconUrl = url.substring(0, index) + "/favicon.ico"
                Boolean favIconExits = !favIconUrl.contains("https") ? false : checkIfFavIconExists(favIconUrl)
                return favIconExits ? favIconUrl : ""
            }
        }
        return ""
    }

    def addReference(Map params, HttpServletRequest request) {
        boolean isUpdateRequest = false;
        User currentUser = userService.getUser()
        String posterClass = currentUser.class.toString()
        Long posterId = currentUser.id
        String oldFileName = null

        References reference = null;
        if (params.refrenceId != null) {
            reference = References.findById(params.refrenceId)
            isUpdateRequest = true;
            oldFileName = reference?.attachment?.name
            oldFileName = oldFileName + "." + reference?.attachment?.ext
        } else {
            reference = new References()
        }

        reference.dateCreated = (isUpdateRequest) ? reference.dateCreated : new Date()
        reference.lastUpdated = new Date()
        reference.description = params.description

        AttachmentReference attachment = (isUpdateRequest) ? reference.getAttachment() : new AttachmentReference()
        if (null == params.attachments && "undefined" != params.attachments) {
            attachment.name = (isUpdateRequest && params.fileTypeChecked == true) ? attachment.name : params.referenceLink
            attachment.posterClass = posterClass
            attachment.length = 0L
            attachment.posterId = (isUpdateRequest) ? attachment.posterId : posterId
            attachment.referenceType = params.refrenceType
            attachment.attachmentType = (params.fileTypeChecked == true) ? attachment.attachmentType : AttachmentType.Reference
            attachment.referenceLink = (isUpdateRequest && params.fileTypeChecked == true) ? attachment.referenceLink : params.referenceLink
            reference.favIconUrl = (isUpdateRequest && params.fileTypeChecked == true) ? getFavIconUrl(attachment.referenceLink) : getFavIconUrl(params.referenceLink)
            attachment.inputName = (params.inputName == null || params.inputName == "") ? params.referenceLink : params.inputName
            attachment.ext = params.linkTypeTypeChecked == true ? null : FilenameUtils.getExtension( params.inputName )
        } else if("undefined" == params.attachments && isUpdateRequest && params.inputName) {
            attachment.inputName = params.inputName
            attachment.ext = params.linkTypeTypeChecked == true ? null : FilenameUtils.getExtension( params.inputName )
        } else {
            def fileName = params?.attachments?.filename
            if (fileName instanceof String) {
                fileName = [fileName]
            }
            String inputFileName = null
            List nameList = []
            if (params.fileName) {
                inputFileName = params.fileName
            } else {
                inputFileName = fileName.get(0)
            }
            nameList = isFileSavedNameAlreadyExits([inputFileName])
            if (nameList.size() >= 1) {
                inputFileName = generateNewFileName1(nameList.get(0), isUpdateRequest, oldFileName)
            }
            Map filesStatusMap = uploadFileTo(currentUser, fileName, request, params, attachment, isUpdateRequest, params.inputName, params.attachmentType, inputFileName)
            def attachmentsSize = 0 - filesStatusMap?.uploadedFiles?.size()
            String fileDescription = params.description
            attachment = filesStatusMap.get("attachmentReference")
            attachment.ext = params.linkTypeTypeChecked == true ? null : FilenameUtils.getExtension( inputFileName )
        }

        reference.attachment = attachment
        if (!isUpdateRequest) {
            reference.createdBy = currentUser.fullName
        }
        reference.modifiedBy = currentUser.fullName
        if(!reference?.shareWithUser?.contains(currentUser))
             reference.addToShareWithUser(currentUser)
        if (isUpdateRequest) {
            CRUDService.update(reference)
        } else {
            reference.save(flush: true)
        }
        if (!isUpdateRequest) {
            UserReferencesMapping userReferencesMapping = new UserReferencesMapping()
            userReferencesMapping.userId = currentUser.id as Long
            userReferencesMapping.isPinned = false
            userReferencesMapping.referenceId = reference.id
            userReferencesMapping.isDeleted = false
            Integer priority = UserReferencesMapping.findAllByUserIdAndIsDeleted(currentUser.id, false)?.size()
            userReferencesMapping.priority = priority ? (priority + 1) : 1
            userReferencesMapping.save(flush: true)
        }
    }

    List<Map> getReferencesIds(Long userId, Boolean isPinned, Map params, Integer length = null) {
        String sort = params.sort
        String direction = params.direction
        String sql = SignalQueryHelper.get_references_sql(userId, isPinned, isPinned?"":sort, isPinned?"":direction, length ?: 25, 0, params.searchString)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.addScalar("referenceId", new LongType())
        sqlQuery.addScalar("pinned", new BooleanType())
        sqlQuery.addScalar("priority", new IntegerType())
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        sqlQuery.list()
    }

    List createReferencesMap(List<Map> userReferencesMappingList) {
        String timezone = userService.user.preference.timeZone
        List<User> users = []
        users.add(userService.user)
        boolean isShareEnabled = false
        isShareEnabled = userService.hasAccessShareWith()
        Integer priority = UserReferencesMapping.findAllByUserIdAndIsDeletedAndIsPinned(userService.user.id, false, true)?.size()
        List referenceList = userReferencesMappingList?.collect { it ->
            References reference = References.get(it.referenceId as Long)
            String sharedWithUsers = ''
            String sharedWithGroup = ''
            if (reference.shareWithUser) {
                reference.shareWithUser.remove(null)
                reference.shareWithUser.each {
                    sharedWithUsers = sharedWithUsers + "," + it.username
                }
            }
            if (reference.shareWithGroup) {
                reference.shareWithGroup.remove(null)
                reference.shareWithGroup.each {
                    sharedWithGroup = sharedWithGroup + "," + it.name
                }
            }

            AttachmentReference attachmentReference = reference.getAttachment()
            String inputNameSimplified = attachmentReference.inputName?.replaceAll("\"","&#34;")
            String referenceLinkSimplified = attachmentReference.referenceLink?.replaceAll("\"","&#34;")
            //this is done to prevent saving of replaced string in database as the object is saved when we get any property of that object after
            //assignment of any field PVS-54029
            [
                    id             : attachmentReference.id,
                    refId          : reference.id,
                    link           : (referenceLinkSimplified) ? referenceLinkSimplified : inputNameSimplified,
                    type           : attachmentReference.attachmentType.id,
                    referenceType  : attachmentReference.referenceType,
                    description    : reference?.description ?: Constants.Commons.BLANK_STRING,
                    timeStamp      : (reference.lastUpdated != null) ? DateUtil.stringFromDate(reference.lastUpdated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone) : null,
                    modifiedBy     : reference.modifiedBy ? reference.modifiedBy : reference?.createdBy,
                    inputName      : inputNameSimplified,
                    isShareEnabled : isShareEnabled,
                    name           : attachmentReference.savedName,
                    sharedWithUser : sharedWithUsers,
                    sharedWithGroup: sharedWithGroup,
                    addedBySelf    : reference.createdBy == userService.user.fullName || (userService.user?.isAdmin()),
                    priority       : it.priority,
                    isPinned       : it.pinned,
                    favIconUrl     : reference.favIconUrl ?: "",
                    fileType       : attachmentReference.ext ?: ""
            ]
        }
        return referenceList
    }

    Integer getTotalRecords(Map params){
        Session session = sessionFactory.currentSession
        String totalCountSql = SignalQueryHelper.get_ref_count_sql(userService.user.id, params.searchString)
        Integer totalCount = alertService.getResultListCount(totalCountSql, session)
        return totalCount
    }

    List fetchReferences(Map params) {
        Integer length = params.length as Integer
        List pinnedRefIds = getReferencesIds(userService.user.id, true, params, length)
        Integer refIdLength = length?(length-pinnedRefIds.size()):(25-pinnedRefIds.size())
        List refIds = getReferencesIds(userService.user.id, false, params, refIdLength)
        return createReferencesMap(pinnedRefIds) + createReferencesMap(refIds)
    }

    List isFileSavedNameAlreadyExits( List<String> fileNames) {
        AttachmentReference.findAllByAttachmentType(AttachmentType.Attachment).collect {
            it.savedName
        }.intersect(fileNames)
    }
    def uploadFileTo(User user, List inputNames, MultipartHttpServletRequest request,Map params,AttachmentReference attachment,boolean isUpdateRequest,String inputName=null, String attachmentType=null, String fileName = null, String rmmFileName = null) {
        List<MultipartFile> filesToUpload = []
        Map uploadedFiles=[:]
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
            uploadedFiles = upload(user, filesToUpload, params, attachment, isUpdateRequest,inputName,attachmentType,fileName,rmmFileName)
        }

        [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles.get("uploadedFiles"),attachmentReference: uploadedFiles.get("attachmentReference")]
    }

    String generateNewFileName1(String fileName,boolean isUpdateRequest,String oldFileName)   {

        int extensionIndex = fileName.lastIndexOf(".")
        String extension = fileName.substring(extensionIndex)
        String result = fileName
            int startIndex = (fileName.substring(0,extensionIndex)).lastIndexOf("(")
            if(startIndex < extensionIndex && fileName.substring(startIndex+1,extensionIndex-1).isInteger()){
                if(fileName.equals(oldFileName) && isUpdateRequest){
                    result = fileName
                }
                else{
                    int sequence = fileName.substring(startIndex+1,extensionIndex-1).toInteger()+1
                    result = fileName.substring(0,startIndex)+"(${sequence})${extension}"
                }

            }

        return result
    }

    String generateNewFileName(String fileName)   {
        int extensionIndex = fileName.lastIndexOf(".")
        String extension = fileName.substring(extensionIndex)
        String result = fileName
        if(fileName.charAt(extensionIndex-1)==")" && fileName.contains("(")){
            int startIndex = (fileName.substring(0,extensionIndex)).lastIndexOf("(")
            if(startIndex < extensionIndex && fileName.substring(startIndex+1,extensionIndex-1).isInteger()){
                int sequence = fileName.substring(startIndex+1,extensionIndex-1).toInteger()+1
                result = fileName.substring(0,startIndex)+"(${sequence})${extension}"
            }
        } else {
            result = fileName.substring(0,extensionIndex)+"(1)${extension}"
        }
        List nameList = isFileSavedNameAlreadyExits([result])
        if (nameList.size()>=1){
            result = generateNewFileName(nameList.get(0))
        }
        return result
    }
    Map upload(User poster,

    List<MultipartFile> files,Map params,AttachmentReference attachment,boolean isUpdateRequest,String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null) {
        def uploadedFiles = []
        def map=[:]
        AttachmentReference attachmentReference;
        try {
            Attachment.withTransaction {status ->
                Integer filesCount = files.size()
                files.each {MultipartFile file ->
                    try {
                        checkExtension(file.originalFilename)
                        attachmentReference= addAttachment(poster, file, params, attachment, isUpdateRequest,inputName,attachmentType,fileName,rmmFileName)
                        map.put("attachmentReference",attachmentReference)
                        uploadedFiles << file
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
        map.put("uploadedFiles",uploadedFiles)
        return map;

    }
    void checkExtension(String fileName) {
        List formatsNotSupported = Holders.config.signal.file.not.accepted.formats
        List formatsSupported = Holders.config.signal.file.accepted.formats
        formatsSupported = formatsSupported.minus(formatsNotSupported)
        if(!formatsSupported.contains('.' + FilenameUtils.getExtension(fileName)?.toLowerCase())) {
            throw new FileFormatException("File Format is not correct")
        }
    }
    def addAttachment(User poster, MultipartFile file,Map params,AttachmentReference attachment,boolean isUpdateRequest,String inputName=null,String attachmentType=null,String fileName = null,String rmmFileName = null) {
       return doAddAttachment(grailsApplication.config, poster, file, params, attachment, isUpdateRequest,inputName,attachmentType,fileName,rmmFileName)
    }

    private AttachmentReference doAddAttachment(def config,
                                   User poster,

                                   MultipartFile file,Map params,AttachmentReference attachmentObj,boolean isUpdateRequest,String inputName=null,String attachmentType=null,String inputFileName= null,String rmmFileName = null) {



        if (!file?.size) {
            throw new EmptyFileException(file.name, file.originalFilename)
        }
        //String delegateClassName = AttachmentableUtil.fixClassName(reference.class)
        String posterClass = (poster instanceof String) ? poster : AttachmentableUtil.fixClassName(poster.class.name)
        Long posterId = (isUpdateRequest)?attachmentObj.posterId:poster.id
        String filename = file.originalFilename
        String savedName = getRandomNameFile()
        AttachmentReference attachment
        if(isUpdateRequest && attachmentObj){
            attachmentObj.setProperties([
                    name: FilenameUtils.getBaseName(filename),
                    ext: FilenameUtils.getExtension(filename),
                    length: 0L,
                    contentType: file.contentType,
                    // poster
                    posterClass: posterClass ?: "",
                    posterId: posterId
            ]) as AttachmentReference
            attachment = attachmentObj
        }else{
            attachment = new AttachmentReference(
                    // file
                    name: FilenameUtils.getBaseName(filename),
                    ext: FilenameUtils.getExtension(filename),
                    length: 0L,
                    contentType: file.contentType,
                    // poster
                    posterClass: posterClass ?: "",
                    posterId: posterId,

                    // input
            )
        }
        attachment.referenceType=params.refrenceType

        if(inputFileName){
            attachment.ext =inputFileName.substring(inputFileName.indexOf(".")+1)
            attachment.savedName = inputFileName
        } else {
            attachment.savedName =savedName
            attachment.ext =FilenameUtils.getExtension(filename)
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
                attachment.inputName = rmmFileName
            } else {
                attachment.inputName = filename
            }
        }
        File diskFile = AttachmentableUtil.getFileForReference(config, attachment, true)
        file.transferTo(diskFile)
        checkType(diskFile)
        attachment.length = diskFile.length()
        return attachment;
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
    String getRandomNameFile() {
        String state = UUID.randomUUID().toString()
        return state
    }
    @Transactional
    boolean deleteReferences(Long refId){
        Long loginUserId=userService.user.id;
        Boolean isPinned = UserReferencesMapping.findByUserIdAndReferenceId(loginUserId, refId)?.isPinned ?: false
        Boolean isDeleted = true
        References references=References.findById(refId);
        AttachmentReference attachmentRef=references?.attachment;
        if(attachmentRef.posterId==loginUserId){
            references.shareWithGroup=[]
            references.shareWithUser=[]
            references.isDeleted=true
            UserReferencesMapping.executeUpdate("UPDATE UserReferencesMapping SET isDeleted=:isDeleted WHERE referenceId =:refId", [refId:refId, isDeleted:isDeleted])
        }
        else{
           if(references.shareWithUser!=null && references.shareWithUser.isEmpty()==false){
               List<User> shareWithUserObj=[];
               references.shareWithUser.each {
                  if(it.username!=userService.user.username){
                       shareWithUserObj.add(it);
                   }
               }
               references.shareWithUser= shareWithUserObj;
           }
            if(references.shareWithGroup!=null && references.shareWithGroup.isEmpty()==false){
                List<User> shareWithUserObj=[];
                shareWithUserObj.add(userService.user);
                references.deletedByUser= shareWithUserObj;
            }
            UserReferencesMapping.executeUpdate("UPDATE UserReferencesMapping SET isDeleted=:isDeleted WHERE referenceId =:refId and userId =:userId", [refId:refId, userId: loginUserId, isDeleted:isDeleted])
        }
        updatePriority(isPinned)
        return true
    }
    def getSharedWith(Long referenceId){
        def response=[:]
        if(referenceId){
            References reference=References.get(referenceId)
            Set<Map> users = reference.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]}
            List<Map>  groups= reference.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]}
            List<Map> all=[]

            all.addAll(users)
            all.addAll(groups)
            response.put("users",users)
            response.put("groups",groups)
            response.put("all",all)
        }
        return response

    }
    @Transactional
    def updateRefrencesPriority(List updatedRows, Boolean isPinned){
        updatedRows = updatedRows.reverse()
        Long userId = userService.user.id
        updatedRows.eachWithIndex{ def referenceId, Integer itr ->
            UserReferencesMapping.executeUpdate("UPDATE UserReferencesMapping SET isPinned =:isPinned, priority= :itr WHERE referenceId= :referenceId AND userId= :userId", [isPinned:isPinned, itr:itr+1, referenceId:referenceId as Long,userId:userId as Long])
        }
        return true
    }
    @Transactional
    void updatePriority(Boolean isPinned){
        User currentUser = userService.getUser()
        List refIds = getReferencesIds(currentUser.id, isPinned, [:], -1)
        refIds = refIds.sort { it.priority }
        refIds.eachWithIndex { map, itr ->
            UserReferencesMapping.executeUpdate("UPDATE UserReferencesMapping SET isPinned=:isPinned, priority=:priority WHERE referenceId= :referenceId AND userId= :userId", [isPinned: isPinned, referenceId: map.referenceId as Long, userId: currentUser.id, priority: itr + 1])
        }
    }

    @Transactional
    def pinReferences(Map params) {
        User currentUser = userService.getUser()
        Boolean isPinned = (params.isPinned == "true" || params.isPinned == true) ? true : false
        Integer priority = UserReferencesMapping.findAllByUserIdAndIsDeletedAndIsPinned(currentUser.id, false, isPinned ? true : false)?.size()
        priority = priority ? (priority + 1) : 1
        updatePriority(isPinned)
        UserReferencesMapping.executeUpdate("UPDATE UserReferencesMapping SET isPinned=:isPinned, priority=:priority WHERE referenceId= :referenceId AND userId= :userId", [isPinned: isPinned, referenceId: params.refId as Long, userId: currentUser.id, priority: priority])
        return true
    }

}
