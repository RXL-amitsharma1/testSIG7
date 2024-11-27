
var parentRow;
$(function () {


    $("#disassociationJustificationPopover a.selectJustificationNew").hover(
        function () {
            $('.tooltip').not(this).hide();
        }, function () {
            $('.tooltip').not(this).show();
        }
    );

    $("#disassociationJustificationPopover a.addNewJustificationForDisassociation").hover(
        function () {
            $('.tooltip').not(this).hide();
        }, function () {
            $('.tooltip').not(this).show();
        }
    );

    $('#disassociationJustificationPopover #edit-boxDis').hide();

    $(document).on('click', '.changeDisassociation', setDisassociationTriggerButton);


    $('#disassociationJustificationPopover .selectJustificationNew').off('click').on('click', function (e) {
        e.preventDefault();
        if (e.which == 1) {
            initChangeDisassociation($(this).html());
            disassociationEditPadding();

        }

    });

    $('#disassociationJustificationPopover #addNewJustificationForDisassociation, #disassociationJustificationPopover .editIconDis').on('click', function (e) {
        if ($(e.target).hasClass('editIconDis')) {
            $('#disassociationJustificationPopover .editedJustificationForDisassociation').val($(this).parent().siblings('.selectJustificationNew').html());
            disassociationEditPadding();
        }
        $('#disassociationJustificationPopover #addNewJustificationForDisassociation').hide();
        $('#disassociationJustificationPopover #edit-boxDis').show();
        disassociationEditPadding();
    });

    $('#disassociationJustificationPopover #cancelJustificationForDisassociation').on('click', function () {
        if($("#dueDate").val().length>0){
            addRemovePreviouslyAddedJustificatin('SHOW')
        }
        $(".countBox").empty()
        $(".countBox").append("0/4000")
        $("#disassociationJustificationPopover").hide()
        $("#dueDate").val("")
        clearAndHideDisassociationJustificationEditBox();
    });

    $('#disassociationJustificationPopover #confirmJustificationForDisassociation').on('click', function () {
       var length=$(".editedJustificationForDisassociation").val().length;
       var lengthDueDate= $("#dueDate").val().length;
        if (length > 8000) {
            $.Notification.notify('error','top right', "Error", "Justification Length is too long.", {autoHideDelay: 5000});
            return false;
        }
        if(lengthDueDate>0){
            if(length<1){
                $.Notification.notify('warning', 'top right', "Warning", $.i18n._('justification_saveWarning'), {autoHideDelay: 5000}); //converted error message to warning message to sync up with PVS-47979
                return false;
            }
        }
        $("#disassociationJustificationPopover").hide();
       initChangeDisassociation($('#disassociationJustificationPopover .editedJustificationForDisassociation').val());

    });

    $(".modalCloseBTN").on('click', function (){
      if($("#dueDate").val().length>0){
           $("#disassociationJustificationPopover").hide()
           $("#dueDate").val("")
           $(".dueDateClass").hide();
           addRemovePreviouslyAddedJustificatin('SHOW');
           $(".editButtonEvent").show();
           $("#cancelJustificationForDisassociation").trigger('click');
       }else{
           $("#disassociationJustificationPopover").hide()
       }
    })
});
function disassociationEditPadding() {
    var heightOfDiv = $('.popover.justification ul.text-list > li:last-child').height();
    var paddingBottom = heightOfDiv + 10 + 'px';
    $('.popover.justification ul.text-list').css('padding-bottom', paddingBottom);
    $('.editedJustificationForDisassociation').focus();
}
var clearAndHideDisassociationJustificationEditBox = function () {
    $('#disassociationJustificationPopover #edit-boxDis').hide();
    $('#disassociationJustificationPopover .editedJustificationForDisassociation').val('');
    $('#disassociationJustificationPopover #addNewJustificationForDisassociation').show();
    disassociationEditPadding();
};

var initChangeDisassociation = function (justificationTextInput) {
    $('.changeDisassociation').prop('disabled', true);
    $("#disassociationJustificationPopover").hide();
    clearAndHideDisassociationJustificationEditBox();


    var alertId =(parentRow==undefined)?{}: parentRow.find('span[data-field="removeSignal"]').attr("data-id");
    var appType =(parentRow==undefined)?{}: parentRow.find('span[data-field="workflowState"]').attr("data-apptype");
    var signalName=$("#validatedSignalName").html()
    var signalId=$("#signalIdPartner").val()
    var dueDate=$("#dueDate").val();
    var isArchivedValue=(parentRow==undefined)?{}:parentRow.find('span[data-field ="isArchived"]').attr("data-id");


    var productJson=JSON.stringify(getProductJsonValue());
    $.ajax({
        url:"disassociateFromSignal",
        type:"POST",
        data:{"alertType":appType,"alertId":alertId,"signalName":signalName,"justification":justificationTextInput,"productJson":productJson,"isArchived":isArchivedValue,"signalId":signalId,"dueDate":dueDate},
    })
        .done(function(response){
            if(response.data!=null && response.data!=undefined ){
               if(response.data.dueIn!=null){
                   $("#dueInHeader").empty()
                   $("#dueInHeader").append(response.data.dueIn+" Days")
                   $("#dueDatePicker").hide();
                   $(".editButtonEvent").show();
                   $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                   updateSignalHistory()
                   $.Notification.notify('success', 'top right', "Success", "Due date updated successfully.", {autoHideDelay: 5000});
               }else{
                   $("#dueDatePicker").hide();
                   $(".editButtonEvent").hide();
               }
            }else{
                $.Notification.notify('success', 'top right', "Success", "Dissociated from signal successfully.", {autoHideDelay: 5000});
            }
            if($("#dueDate").val().length>0){
                $("#disassociationJustificationPopover").hide()
                $("#dueDate").val("")
                $(".dueDateClass").hide();
                addRemovePreviouslyAddedJustificatin('SHOW')
               $("#cancelJustificationForDisassociation").trigger('click');
            }
            $("#dueDate").val("")
            $(".dueDateClass").hide();
            $("#divPaddingId").removeAttr("style")

            if(appType=="Aggregate Case Alert" || appType=="EVDAS Alert"){tableAggReview.ajax.reload();}
            else if(appType=='Single Case Alert'){tableSingleReview.ajax.reload();}
            else if(appType== "Literature Search Alert"){tableLiteratureReview.ajax.reload();}
            else{$('#rxTableAdHocReview').DataTable().ajax.reload();}
            $('#signalActivityTable').DataTable().ajax.reload();

        })
        .fail(function (exception, message) {
            console.log(exception)
            if(message){
                $.Notification.notify('error','top right', "Error", "Due date cannot be past date.", {autoHideDelay: 5000});
            } else{
                $.Notification.notify('error','top right', "Error", "An Error occurred while Processing request.", {autoHideDelay: 5000});
            }
        });
    $('.changeDisassociation').prop('disabled', false);



};
var getProductJsonValue=function () {
    var productName =(parentRow==undefined)?'': parentRow.find('span[data-field="productName"]').attr("data-id");
    var productId = (parentRow==undefined)?'':parentRow.find('.row-product-id').val();
    var level =(parentRow==undefined)?'': parentRow.find('.row-level-id').val();
    if (!level) {
        level = 3;
    }

    var topicProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    topicProductValues[level].push({name: productName, id: productId});

    return topicProductValues

}
var setDisassociationTriggerButton=function (event) {
    parentRow=$(event.target).closest('tr');
    $('#disassociationJustificationPopover .popover-title').html(' Justification ');
    var elementLeft=$(this).parent().position().left
    var topValue=event.pageY-50;
    var totalx=$('#disassociationJustificationPopover').width()
    var leftValue=event.pageX  - totalx-50;

    $("#disassociationJustificationPopover").css({'left':leftValue+'px','top':topValue+'px'})
    $("#disassociationJustificationPopover").toggle();


}

var  addRemovePreviouslyAddedJustificatin=function (option){
    var arr=[];
    arr=$(".text-list li");
    for(var i=0;i<arr.length;i++){
        var child=$(arr[i]).children()[0]
        if($(child).attr('id')=='edit-boxDis'){
            break;
        }
        else{
            if('HIDE'==option){
                $(arr[i]).hide();
            }else
            {
                $(arr[i]).show();
            }

        }
    }
}