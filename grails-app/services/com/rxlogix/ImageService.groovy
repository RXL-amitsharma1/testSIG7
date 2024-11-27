package com.rxlogix

import asset.pipeline.grails.AssetResourceLocator
import grails.gorm.transactions.Transactional
import grails.util.Holders

@Transactional
class ImageService {

    def assetResourceLocator
    def grailsApplication

    def InputStream getImage(String filename) {

        InputStream inputStream

        File file = new File((Holders.config.externalDirectory as String) + filename)

        if (file?.exists() && file?.size() > 0) {
            //Show the user provided version
            inputStream = file.newInputStream()
        } else {
            //Show the version we deliver with the app
            inputStream = assetResourceLocator.findAssetForURI(filename).inputStream
        }

        inputStream
    }
}
