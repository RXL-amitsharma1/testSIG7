package com.rxlogix.config

class PVSState implements Serializable {
    static auditable = true

    Long id
    String value
    Boolean display = true
    Boolean finalState = false
    String description
    String description_local
    String displayName
    String displayName_local
    Integer reviewPeriod

    static mapping = {
        table('PVS_STATE')
    }

    static constraints = {
        description nullable: true
        description_local nullable: true
        displayName_local nullable: true
        value unique: true
        reviewPeriod nullable: true
    }

    static belongsTo = []

    @Override
    def String toString() { displayName }

    def toDto() {
        [
                id         : this.id,
                value      : this.value?.trim()?.replaceAll("\\s{2,}", " "),
                description: this.description,
                displayName: this.displayName?.trim()?.replaceAll("\\s{2,}", " "),
                display    : this.display?.trim()?.replaceAll("\\s{2,}", " "),
                finalState : this.finalState
        ]
    }
}