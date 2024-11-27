package com.rxlogix.config

class ReportFieldGroup {

    String name
    boolean isDeleted = false
    boolean isEudraField = false


    static mapping = {
        cache: "read-only"
        version: false

        table name: "RPT_FIELD_GROUP"
        name column: "NAME"
        isDeleted column: "IS_DELETED"
        id name: "name", generator: "assigned"
        isEudraField column : "IS_EUDRAFIELD"
    }

    static constraints = {
        name(unique: true, blank: false)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ReportFieldGroup)) return false

        ReportFieldGroup that = (ReportFieldGroup) o
        if (name != that.name) return false
        if (isEudraField != that.isEudraField) return false
        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (isEudraField ? 1 : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
