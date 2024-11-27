package com.rxlogix

import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseLiteratureAlert {
    //Results after pubmed call
    int articleId
    String articleAuthors
    String articleAbstract
    String articleTitle
    String publicationDate                //not 'Date' type because ..some dates just have year, or month and year

    //Alert Related items
    String name
    String productSelection
    String eventSelection
    String searchString
    String createdBy
    String modifiedBy
    Integer actionCount = 0

    Date dateCreated
    Date lastUpdated
    boolean isDispChanged = false
    String dispPerformedBy

    static constraints = {
        articleAbstract(nullable: true)
        articleTitle(nullable: true)
        articleAuthors(nullable: true)
        articleId(nullable: true)
        productSelection(nullable: true)
        eventSelection(nullable: true)
        createdBy nullable: true
        modifiedBy nullable: true
        actionCount nullable: true
        publicationDate(nullable: true)
        isDispChanged nullable: true
        dispPerformedBy nullable: true
    }

    static mapping = {
        articleAbstract column: 'ARTICLE_ABSTRACT', sqlType: DbUtil.longStringType
        articleAuthors column: "ARTICLE_AUTHORS", sqlType: DbUtil.longStringType
        articleTitle column: "ARTICLE_TITLE", sqlType: DbUtil.longStringType
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        publicationDate column: "PUBLICATION_DATE"
    }

    @Override
    String toString(){
        "$name"
    }
}