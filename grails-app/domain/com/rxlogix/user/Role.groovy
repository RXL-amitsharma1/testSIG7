package com.rxlogix.user
import com.rxlogix.util.ViewHelper


class Role {
    static auditable = false

	String authority
	String authorityDisplay
    String description

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        cache true
        table name: "ROLE"
        authority column: "AUTHORITY"
        authorityDisplay column: "AUTHORITY_DISPLAY"
        description column: "DESCRIPTION"
    }

    static constraints = {
		authority nullable: false, unique: true, maxSize: 50
        authorityDisplay nullable: true, maxSize: 255
        description nullable: true, maxSize: 200
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
	}

    @Override
    String toString() {
        ViewHelper.getMessage("app.role.${this.authority}")
    }
}
