package com.rxlogix.signal

class ProductTypeConfiguration {
    static auditable = [ignore:['productTypeId','roleTypeId','isDefault','lastUpdated','dateCreated']]

    String name
    String productType
    Long productTypeId
    String roleType
    Long roleTypeId
    Boolean isDefault = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
        table name: "PRODUCT_TYPE_CONFIGURATION"
    }

    static constraints = {
        name nullable: false
        productType nullable: false
        productTypeId nullable: false
        roleType nullable: false
        roleTypeId nullable: false
        isDefault nullable: false
        dateCreated nullable: false
        lastUpdated nullable: false
    }

    def toDto() {
        [
                id           : this.id,
                name         : this.name,
                productType  : this.productType,
                productTypeId: this.productTypeId,
                roleType     : this.roleType,
                roleTypeId   : this.roleTypeId,
                lastUpdated  : this.lastUpdated
        ]
    }

    def getInstanceIdentifierForAuditLog() {
        return name;
    }

    def getEntityValueForDeletion(){
        return "${name}:Product Type-${productType},Role Type-${roleType}"
    }

}
