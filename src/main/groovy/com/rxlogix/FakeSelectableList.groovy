package com.rxlogix

/**
 * Created by rafal on 12/8/14.
 */
class FakeSelectableList implements SelectableList {

    @Override
    def getSelectableList() {
        return ["AAAA", "BBBB", "CCCCC", "DDDD", "EEEE"]
    }
}
