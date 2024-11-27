package com.rxlogix.config

import com.rxlogix.enums.QueryOperatorEnum

class Expression {
    ReportField reportField
    String value
    QueryOperatorEnum operator

    public boolean equals (Expression other) {
        if (this.reportField != other.reportField
        || this.value != other.value
        || this.operator != other.operator) {
            return false
        }
        return true
    }
}
