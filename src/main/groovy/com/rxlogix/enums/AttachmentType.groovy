package com.rxlogix.enums

enum AttachmentType {
    Reference('Reference'),
    Attachment('Attachment')

    String id

    AttachmentType(id) { this.id = id }
}