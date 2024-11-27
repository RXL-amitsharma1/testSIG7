package com.rxlogix.signal

import com.rxlogix.config.LiteratureAlert

class GlobalArticle {
    Long articleId

    static hasMany = [literatureAlert: LiteratureAlert , pvsGlobalTag : PvsGlobalTag ]

    static mapping = {
        id column: '"globalArticleId"'
        pvsGlobalTag joinTable: [name: "LITERAURE_GLOBAL_TAGS", column: "PVS_GLOBAL_TAG_ID", key: "GLOBAL_ARTICLE_ID"]
    }
}
