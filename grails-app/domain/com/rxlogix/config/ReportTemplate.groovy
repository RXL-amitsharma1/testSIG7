package com.rxlogix.config

import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ReportTemplate implements Comparable, Serializable {

    Long id

    transient def templateService

    String name
    String description
    Category category
    User owner
    boolean isPublic = false
    boolean isDeleted = false
    List tags

    //todo:  this should be removed because we have subclasses of ReportTemplate. By having templateType, it's a duplicate way to specify type (and can be a source of errors if not set carefully) - morett
    //todo:  instanceof should be used instead of this - morett
    TemplateTypeEnum templateType

    int originalTemplateId = 0
    boolean factoryDefault = false

    boolean hasBlanks = false
    //todo:  hasBlanks is only relevant for SQL/Non Case templates. Move to those subclasses. - morett

    ReassessListednessEnum reassessListedness

    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static hasMany = [tags: Tag]   //Standard fields

    static mapping = {
        tablePerHierarchy false
        table name: "RPT_TEMPLT"

        tags joinTable: [name: "RPT_TEMPLTS_TAGS", column: "TAG_ID", key: "RPT_TEMPLT_ID"], indexColumn: [name: "TAG_IDX"]

        name column: "NAME"
        description column: "DESCRIPTION"
        category column: "CATEGORY_ID"
        owner column: "PV_USER_ID"
        isPublic column: "IS_PUBLIC"
        isDeleted column: "IS_DELETED"
        templateType column: "TEMPLATE_TYPE"
        originalTemplateId column: "ORIG_TEMPLT_ID"

        reassessListedness column: "REASSESS_LISTEDNESS"
        hasBlanks column: "HASBLANKS"
    }

    static constraints = {
        name(nullable: false, maxSize: 200, validator: { val, obj ->
            //Name is unique to user
            if ((!obj.id || obj.isDirty("name")) && obj.originalTemplateId == 0) {
                def existingReportTemplate = ReportTemplate.countByNameAndOwnerAndIsDeleted(obj.name, obj.owner, false)
                if (existingReportTemplate > 0) {
                    return "com.rxlogix.config.template.name.unique.per.user"
                }
            }
        })

        description(nullable: true, maxSize: 200)
        category(nullable: true)
        tags(nullable: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        reassessListedness(nullable: true)
        isPublic(nullable:false)
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser.isAdmin() || (owner == currentUser))
//        return (currentUser.isAdmin() || createdBy == currentUser.username)
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isAdmin() || owner == currentUser || isPublic)
    }

    def countUsage() {
        def count = 0
        def list = Configuration.findAllByIsDeleted(false)
        list.collect {
            it.templateQueries.collect {
                if (it.template.id == this.id) {
                    count++
                }
            }
        }
        return count
    }

    def getNameWithDescription() {
        return this.name + " " + (this?.description ? "(" + this.description + ")" : "")
    }

    List<ReportFieldInfo> getAllSelectedFieldsInfo() { [] }

    List sortInfo() {
        List result = []
        if (templateType == TemplateTypeEnum.CASE_LINE || templateType == TemplateTypeEnum.DATA_TAB) {
            List sortFields = []

            getAllSelectedFieldsInfo().eachWithIndex { rfInfo, index ->
                if (rfInfo?.sortLevel > 0) {
                    sortFields.add([rfInfo, index])
                }
            }

            if (sortFields.size() > 0) {
                sortFields.sort { it[0].sortLevel }
                sortFields.each {
                    result.add([it[1], it[0].sort.value()])
                }
            }
        }

        return result
    }

    int compareTo(def obj) {
        //ascending order
        int value = name <=> obj?.name
        return value
    }
    @Override
    String toString() {
        "$name"
    }

}
