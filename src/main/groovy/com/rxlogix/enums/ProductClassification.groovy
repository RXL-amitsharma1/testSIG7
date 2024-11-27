package com.rxlogix.enums

enum ProductClassification {

    DRUG("Drug"),
    VACCINCE("Vaccine")

    def id

    ProductClassification(id) {this.id = id}

    static ProductClassification getValue(String value){
        com.rxlogix.enums.ProductClassification productClassification
        productClassification = values().find {it.toString() == value}
        productClassification
    }
}