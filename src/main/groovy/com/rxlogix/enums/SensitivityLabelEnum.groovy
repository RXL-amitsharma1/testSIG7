package com.rxlogix.enums

enum SensitivityLabelEnum {

    CONFIDENTIAL("Confidential", "label-confidential.png"),
    PROPRIETARY("Proprietary", "sensitivity-label-proprietary.png"),
    PUBLIC("Public", "sensitivity-label-public.png"),
    SENSITIVE("Sensitive", "sensitivity-label-sensitive.png")

    final String value
    final String imageName

    SensitivityLabelEnum(String value, String imageName){
        this.value = value
        this.imageName = imageName
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.sensitivityLabel.${this.name()}"
    }
}