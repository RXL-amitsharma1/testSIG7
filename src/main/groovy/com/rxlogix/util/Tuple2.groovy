package com.rxlogix.util

class Tuple2 implements Serializable {
    def v1
    def v2

    Tuple2(pv1, pv2) {
        this.v1 = pv1
        this.v2 = pv2
    }

    def getValue(idx) {
        idx == 1 ? v1 : v2
    }

    def getFirst() { getValue(1) }

    def getSecond() { getValue(2) }
}
