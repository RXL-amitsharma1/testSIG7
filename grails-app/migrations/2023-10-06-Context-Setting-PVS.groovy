import com.rxlogix.ContextSettingPVSService

databaseChangeLog = {

    changeSet(author: "hritik (generated)", id:"1696831316-1") {
        grailsChange{
            change{
                try{
                    ContextSettingPVSService contextSettingPVSService = ctx.getBean("contextSettingPVSService")
                    contextSettingPVSService.serviceMethod()
                } catch (Exception ex){
                    ex.printStackTrace()
                }
            }
        }
    }
}