package com.rxlogix

import grails.gorm.dirty.checking.DirtyCheck
import groovy.transform.EqualsAndHashCode

@DirtyCheck
@EqualsAndHashCode(includes = ['productName', 'pt'] )
abstract class BaseAggregateAlert {

    String name

    //Product Event Information
    String productName
    BigInteger productId
    String pt
    Integer ptCode
    String soc

    Date periodStartDate
    Date periodEndDate

    //Aggregated data
    Integer newStudyCount
    Integer cumStudyCount
    Integer newSponCount
    Integer cumSponCount
    Integer newSeriousCount
    Integer cumSeriousCount
    Integer newFatalCount
    Integer cumFatalCount

    String positiveRechallenge
    String positiveDechallenge
    String listed
    String related

    //EBGM values
    Double eb95
    Double eb05
    Double ebgm

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String eb95Age
    String eb05Age
    String ebgmAge
    String eb05Gender
    String eb95Gender
    String ebgmGender

    String smqCode
    Double chiSquare

    String rorSubGroup
    String rorLciSubGroup
    String rorUciSubGroup
    String prrSubGroup
    String prrLciSubGroup
    String prrUciSubGroup
    String ebgmSubGroup
    String eb05SubGroup
    String eb95SubGroup
    String chiSquareSubGroup
    String rorRelSubGroup
    String rorLciRelSubGroup
    String rorUciRelSubGroup


    boolean flagged = false

    static constraints = {
        periodStartDate nullable: true
        periodEndDate nullable: true
        related nullable: true
        listed nullable: true
        positiveRechallenge nullable: true
        positiveDechallenge nullable: true
        smqCode nullable: true
        eb95Age nullable: true
        eb05Age nullable: true
        ebgmAge nullable: true
        eb05Gender nullable: true
        eb95Gender nullable: true
        ebgmGender nullable: true
        chiSquare nullable: true
        rorSubGroup nullable: true
        rorLciSubGroup nullable: true
        rorUciSubGroup nullable: true
        prrSubGroup nullable: true
        prrLciSubGroup nullable: true
        prrUciSubGroup nullable: true
        ebgmSubGroup nullable: true
        eb05SubGroup nullable: true
        eb95SubGroup nullable: true
        chiSquareSubGroup nullable: true
        rorRelSubGroup nullable:true
        rorLciRelSubGroup nullable:true
        rorUciRelSubGroup nullable:true
    }

    @Override
    String toString(){
        "$name"
    }

}
