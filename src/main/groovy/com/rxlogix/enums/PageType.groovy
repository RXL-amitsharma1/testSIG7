package com.rxlogix.enums

enum PageType {
    QualitativeAlert(1, "qualitative_alert"),
    CumulativeQualitativeAlert(2, "cumulative_qualitative_alert"),
    QuantitativeAlert(3, "quantitative_alert"),
    CumulativeQuantitativeAlert(4, "cumulative_quantitative_alert"),
    EvdasAlert(5, "evdas_alert"),
    CumulativeEvdasAlert(6, "cumulative_evdas_alert"),
    AdhocAlert(7, "adhoc_alert"),
    SignalManagement(8, "signal_management"),
    LiteratureSearchAlert(9, "literature_search_alert")

    final Integer id
    final String name

    PageType(Integer id, String name) {
        this.id = id
        this.name = name
    }

    static PageType findById(id){
        values().find{it.id == id}
    }

    static PageType findByName(name){
        values().find{it.name == name}
    }
}
