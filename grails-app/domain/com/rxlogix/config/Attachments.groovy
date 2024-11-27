package com.rxlogix.config

class Attachments {

    byte[] sourceAttachments
    String name
    String appType


    static mapping = {
        table name: "FILE_ATTACHMENTS"
    }
    static constraints = {
        name nullable: true
        appType nullable: true
        sourceAttachments sqlType: 'bytea'

    }

    @Override
    String toString() {
        "$name"
    }
}
