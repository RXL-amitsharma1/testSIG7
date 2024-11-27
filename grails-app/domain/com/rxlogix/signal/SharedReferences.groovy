package com.rxlogix.signal

import com.rxlogix.attachments.AttachmentReference
import com.rxlogix.config.Disposition
import com.rxlogix.config.QueryValueList
import com.rxlogix.config.TemplateQuery
import com.rxlogix.user.Group
import com.rxlogix.user.User

class SharedReferences {
    Long id
    References reference;
    List<User> sharedWithUsers;
    List<Group> sharedWithGroup;
    String description
    Date dateCreated
    boolean isDeleted;
    static hasMany = [ sharedWithUsers: User, sharedWithGroup: Group]
    static mapping={

        table name: "SHRED_REFERENCES"

        username column: "USERNAME"
        enabled column: "ENABLED"
        sharedWithUsers joinTable: [name:"SHARE_WITH_USER_REFERENCES", column:"SHARE_WITH_USER_ID", key:"CONFIG_ID"]
        sharedWithGroup joinTable: [name:"SHARE_WITH_GROUP_REFERENCES", column:"SHARE_WITH_GROUP_ID", key:"CONFIG_ID"]
    }
}
