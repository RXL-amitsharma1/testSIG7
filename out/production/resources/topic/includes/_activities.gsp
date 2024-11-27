<asset:javascript src="app/pvs/activity/activities.js"/>
<script>
    $(document).ready(function() {
        var applicationName = "Topic";
        var initializeTopicActivityTable = signal.activities_utils.init_activities_table("#topicActivityTable", activityUrl, applicationName);

        $("#detail-tabs #topicActivities").click(function(event) {
            initializeTopicActivityTable.ajax.reload();
        })
    })
</script>

<div class="rxmain-container ">

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                Activities
            </label>
            <g:render template="/includes/widgets/export_panel"
                      model="[controller :'activity',
                              action     : 'exportTopicActivitiesReport',
                              extraParams:[topicId:topic.id]
                      ]"/>
        </div>
        <div class="rxmain-container-content">

            <table id="topicActivityTable" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th class=""><g:message code="app.label.activity.type" /></th>
                    <th width="50%"><g:message code="app.label.description" /></th>
                    <th><g:message code="app.label.performed.by" /></th>
                    <th><g:message code="app.label.timestamp" /></th>
                </tr>
                </thead>
            </table>

        </div>
    </div>
</div>