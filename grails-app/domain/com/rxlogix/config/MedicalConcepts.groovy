package com.rxlogix.config

class MedicalConcepts implements Serializable {
    static auditable = true

    String name
    Boolean value

    static constraints = {
        value nullable: true
    }
    static mapping = {
        table name: "MEDICAL_CONCEPTS"
    }
}
