package com.rxlogix.enums

enum AlertType {
    SINGLE_CASE_ALERT("Single Case Alert"),
    AGGREGATE_ALERT("Aggregate Alert"),
    EVDAS_ALERT("Evdas Alert")

    def id

    AlertType(id) { this.id = id }
}