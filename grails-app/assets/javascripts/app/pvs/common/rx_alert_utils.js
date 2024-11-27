//= require app/pvs/common/rx_handlebar_ext.js

var signal = signal || {};

signal.alerts_utils = (function() {
    var priorities;
    var workflowStates;

    var get_priorities = function() {
        $.ajax({
            url: "/signal/workflow/priorities",
            async: false,
            dataType: 'json'
        })
            .done(function(result) {
                priorities = result
            });

        return priorities
    };

    var get_workflowStates = function(initData) {
        $.ajax({
            url: "/signal/workflow/workflowState",
            async: false,
            dataType: 'json',
            data: initData,
        })
            .done(function(result) {
                workflowStates = result
            });

        return workflowStates
    };

    var priorities_selections = function(data, type, row) {
        var new_select = '<div><select class="form-control add-cursor" ' +
            'style="height: 30px;" onchange="updatePriority(this.value,' + row.id + ')">';
        var priority = row.priority;

        var the_select = _.reduce(priorities, function(m, p){
            if (p.value == priority)
                return m + '<option value="' + p.value + '" selected> ' + p.displayName + '</option>';
            else
                return m + '<option value="' + p.value + '">' + p.displayName + '</option>'
        }, new_select);
        the_select = the_select + '</select></div>';
        return the_select;
    };

    var workflow_selections = function(data, type, row) {
        var new_select = '<div><select class="form-control add-cursor" style="height: 30px;" onchange="updateStatus(this.value,' + row.id + ')">';
        var workflowState = row.workflowState;

        var the_select = _.reduce(workflowStates, function(m, ws){
            if (ws.value == workflowState)
                return m + '<option value="' + ws.value + '" selected> ' + ws.displayName + '</option>';
            else
                return m + '<option value="' + ws.value + '">' + ws.displayName + '</option>'
        }, new_select);
        the_select = the_select + '</select></div>';
        return the_select;
    };

    var compose_edit_state_link = function(value, rowId, filedName) {
        return "<span>" + value + "</span>" + "<a href='#' class='edit-state' >" +
            "<i class='fa fa-share-alt pull-right' data-field='"  + filedName +
            "' data-id='" + rowId + "'/></a>"
    };

    var state_changed = function(extra_values) {
        $("#valueSelect").on('change', function(evt) {
            var available_extra_values = extra_values[$(valueSelect).val()]
        })
    };

    //TODO this is a temp solution for the JSON format. It should come from server side
    //Function that converts the json innerhtml to the csv values.
    //It converts the properties which are not id.
    var show_json_as_csv = function (elementClass) {
        $("." + elementClass).each(function () {
           try {
              if (isNaN($(this).html())) {
                  var modifiedVal = "";
                  var jsonVal = JSON.parse($(this).html());
                  for (var obj in jsonVal) {
                     if (jsonVal.hasOwnProperty(obj)) {
                        for (var prop in jsonVal[obj]) {
                           if (jsonVal[obj].hasOwnProperty(prop)) {
                              if (prop != 'id') {
                                 modifiedVal = modifiedVal + jsonVal[obj][prop] + ","
                              }
                           }
                        }
                     }
                  }
                  $(this).html(modifiedVal.slice(0, -1))
              }
           } catch (err) {
              //Do nothing
              //console.log(err)
           }
           $(this).html()
        })
    };

    var init_matched_alerts_table = function(tableEle, url) {
        var table = tableEle.DataTable({
            "language": {
                "url": "/signal/assets/i18n/dataTables_" + userLocale + ".json"
            },

            ajax: {
                url: url,
                dataSrc: '',
                error: function(xhr,status,error){}
            },

            aoColumns:[
                {
                    mData: 'name'
                },
                {
                    mData: 'productSelection'
                },
                {
                    mData: 'topic'
                },
                {
                    mData: 'detectedDate'
                },
                {
                    mData: 'disposition'
                }
            ],
        });

        return table
    };

    var getSignalNameList = function(signalsAndTopics) {
        var signalAndTopics = '';
        $.each(signalsAndTopics, function(i, obj){
            var url = signalDetailUrl + '?id=' + obj['signalId'];
            signalAndTopics = signalAndTopics + '<span class="click"><a href="' + url + '">' + obj['name'] + '</a></span>&nbsp;'
            signalAndTopics = signalAndTopics + ","
        });
        if(signalAndTopics.length > 1)
            return '<div>' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
        else
            return '-';
    };

    // For shared with modal
    var initializeShareWithSelect2 = function(){
        $('#shareWith').select2().on("change", function (e) {
            $('#shareWith').parent().removeClass('has-error');
        });
    };

    var initializeShareWithValues = function(){
        $('#sharedWithModal').on('show.bs.modal', function(e) {
            var executedConfigId = e.relatedTarget.id;
            $('#executedConfigId').val(executedConfigId);
            $('#sharedWith').parent().removeClass('has-error');

            $.ajax({
                cache: false,
                type: 'GET',
                url: getSharedWith + '?id=' + executedConfigId,
            })
                .done(function(result) {
                    var users = '';
                    $.each(result.users, function() {
                        users += this.name + '<br />'
                    });
                    $('#sharedWithUserList').html(users);
                    var groups = '';
                    $.each(result.groups, function() {
                        groups +=  this.name + ' <br />'
                    });
                    $('#sharedWithGroupList').html(groups);

                    $.each(result.all, function(i, data){
                        var option = new Option(data.name, data.id, true, true);
                        $(option).attr('data-blinded', data.blinded);
                        $('#sharedWith').append(option).trigger('change');
                    });
                });

            sharedWithModalShow = true;

        }).on('hidden.bs.modal', function(e) {
            sharedWithModalShow = false;
            $('#sharedWith').val(null).trigger('change');
            $('#sharedWith').find('option').remove();
        });
    };

    var getTagsElement = function(tags) {
        var tagsElement = '<div class="tag-container block-ellipsis"><div class="tag-length">';
        var globalTagsArray = [];
        var alertTagsrray = [];
        var privateTagsArray = [];
        $.each(tags, function (key, value) {
            if (value.privateUser == '(P)') {
                privateTagsArray.push(value)
            } else if (value.tagType == '(A)') {
                alertTagsrray.push(value)
            } else {
                globalTagsArray.push(value);
            }
            var subTags = [];

            if(value.subTagText == undefined || value.subTagText == null || value.subTagText == "") {
                subTags = null
            } else{
                subTags = value.subTagText.split(";")
            }
            tagsElement += signal.utils.render('tags_details', {
                key: key,
                value: value,
                tags:tags,
                subTags:subTags
            });
        });
        var viewAllElements = fetchViewAllTags(globalTagsArray , alertTagsrray , privateTagsArray);
        // To hide pencil icon for child case detail
        var childCaseClass = "";
        if(typeof isChildCase !== "undefined" && isChildCase){
            childCaseClass = "hide";
        }

        tagsElement += '<a tabindex="0" data-toggle="tooltip" data-placement="right" title="Add/Edit Categories" class="editAlertTags btn-edit-tag mid pv-hidden-ic '+childCaseClass+'"><i class="mdi mdi-pencil font-20 blue-1"></i></a>';
        tagsElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all1" more-data="'+viewAllElements+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        tagsElement += '</div></div>';
        return tagsElement
    };

    var fetchViewAllTags = function(globalTagsArray , alertTagsArray , privateTagsArray) {
        var returnElement = "";
        var subTags = [];

        if (globalTagsArray.length > 0) {
            returnElement = "<p class='pbr' style='margin-top:7px'><b>Global Category</b><hr class='tagehr'>";
            $.each(globalTagsArray,function(key,value){
                if(value.subTagText == undefined || value.subTagText == null || value.subTagText == "") {
                    subTags = null
                } else{
                    subTags = value.subTagText.split(";")
                }
                value.tagText = escapeAllHTML(value.tagText)
                returnElement += signal.utils.render('tags_details_view_all', {tag: value,subTags: subTags})  ;
            });
            returnElement += "</p>"
        }
        if (alertTagsArray.length > 0) {
            returnElement += "<p class='pbr' style='margin-top:7px'><b>Alert Specific Category</b><hr class='tagehr'>";
            $.each(alertTagsArray,function(key,value){
                if(value.subTagText == undefined || value.subTagText == null || value.subTagText == "") {
                    subTags = null
                } else{
                    subTags = value.subTagText.split(";")
                }
                returnElement += signal.utils.render('tags_details_view_all', {tag: value,subTags: subTags})  ;
            });
            returnElement += "</p>"
        }
        if (privateTagsArray.length > 0) {
            returnElement += "<p class='pbr' style='margin-top:7px'><b>Private Category</b><hr class='tagehr'>";
            $.each(privateTagsArray,function(key,value){
                if(value.subTagText == undefined || value.subTagText == null || value.subTagText == "") {
                    subTags = null
                } else{
                    subTags = value.subTagText.split(";")
                }
                returnElement += signal.utils.render('tags_details_view_all', {tag: value,subTags: subTags})  ;
            });
            returnElement += "</p>"
        }
        return returnElement
    };
    return {
        get_priorities: get_priorities,
        priorities_selections: priorities_selections,
        get_workflowStates: get_workflowStates,
        workflow_selections: workflow_selections,
        compose_edit_state_link: compose_edit_state_link,
        state_change: state_changed,
        show_json_as_csv: show_json_as_csv,
        init_matched_alerts_table: init_matched_alerts_table,
        getSignalNameList: getSignalNameList,
        initializeShareWithSelect2 : initializeShareWithSelect2,
        initializeShareWithValues:initializeShareWithValues,
        get_tags_element : getTagsElement
    }
})();
