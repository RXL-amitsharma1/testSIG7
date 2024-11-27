package com.rxlogix.enums

enum PageOrientationEnum {

    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")

    final String key

    PageOrientationEnum(String key){
        this.key = key
    }

    public getI18nKey() {
        return "app.pageOrientation.${this.name()}"
    }

}