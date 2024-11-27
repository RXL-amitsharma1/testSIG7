var signal = signal || {};

signal.managelinkwidget = (function() {
    var apiurlGetLinks = "dashboard/fetchAttachments",
        apiPinLink = "dashboard/pinReferences",
        apiDelLink= "dashboard/delete",
        apiDragDropLink = "dashboard/dragAndDropRefrences";
    var totalLinkCount;
    var pageNum =1;
    var lastScrollTop = 0;
    var reqLen;
    var showWidgetLoader = function () {
        $(".pv-loader-bg").removeClass('hide');
    }

    var hideWidgetLoader = function () {
        $(".pv-loader-bg").addClass('hide');
    }

    var loadLinks = function (data) {
        var fdata;
        var sdata = data || null;
        if(sdata){
            if(!sdata.hasOwnProperty('qs')){
                fdata= {
                    "sort":sdata.stype,
                    "direction": sdata.dir,
                    "length": sdata.len || reqLen
                };
                if(sdata.hasOwnProperty('scrolling')&& sdata.scrolling){
                    fdata= {
                        "sort":sdata.stype,
                        "direction": sdata.dir,
                        "length": sdata.len
                    };
                }
            } else {
                fdata= {
                    "searchString": sdata.qs,
                    "length": reqLen
                }
            }
        } else{
            var isSorted= $(".link-box-filter button").hasClass("active");
            fdata = {
                "length": reqLen || 50
            };
            if(isSorted){
                fdata.sort = $(".link-box-filter button.active").data("type");
                fdata.direction = $(".link-box-filter button.active").hasClass("dt-ordering-desc")?"desc":"asc";
            }
        }


        $.ajax({
            url: apiurlGetLinks,
            type: 'GET',
            data:fdata,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            beforeSend: function () {
                showWidgetLoader();
            },
        }).done(function (data) {
            if (!$(window).data('ajax_in_progress') === true){
                totalLinkCount =  data.recordsTotal;
            }
            drawLinkBox(data.aaData);
            hideWidgetLoader();
            $(window).data('ajax_in_progress', false);
        }).fail(function (data) {
            $.Notification.notify('error', 'top right', "Error", " Something went wrong! ", {autoHideDelay: 5000});
        })
    }

    var iconClass = function (type) {
        if(type == "pdf"){
            return '<img src="../signal/assets/pdf-icon.png" />';
        } else if(type == "xlx" || type == "xlsx" || type == "csv" || type == "xls"){
            return '<img src="../signal/assets/excel.gif" />';
        } else if(type == "doc" || type == "docx" || type == "csv" || type == "xls"){
            return '<img src="../signal/assets/word-icon.png" />';
        } else if(type == "jpg" || type == "png" || type == "jpeg"){
            return '<img src="../signal/assets/add_annotate.png" />';
        } else if(type == "txt"){
            return '<img src="../signal/assets/word-icon.png" />';
        } else if(type == "ppt"){
            return '<img src="../signal/assets/powerpoint-icon.png" />';
        } else if(type == "pptx"){
            return '<img src="../signal/assets/powerpoint-icon.png" />';
        } else {
            return '<i class="mdi mdi-attachment md-3x"></i>';
        }
    }

    var drawLinkBox = function (data) {
        var html = '';
        $.each(data, function(key, value){
            var favIc;
            if(value.fileType){
                favIc = iconClass(value.fileType.toLocaleLowerCase());
            } else {
                favIc = value.favIconUrl?'<img src="'+value.favIconUrl+'"/>':'<i class="md md-link md-3x"></i>';
            }
            var fileLink = value.fileType?"dashboard/download?id="+value.id:value.link;
            var pinText = value.isPinned? "Unpin":"Pin";
            var classList= value.isPinned? "box-widget listitemClass pinned-box":"box-widget listitemClass";
            var targetLink = value.fileType ? "_self" : "_blank";
            html +=  '<div class= "'+classList+'" id="'+value.id+'" data-id="'+value.refId+'">';
            html +=     '<div class="ic-circle">'+favIc+'</div>';
            html +=     '<span class="dropdown icon-dd">';
            html +=         '<i class="mdi mdi-dots-vertical md-lg dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">';
            html +=         '</i>';
            html +=         '<ul class="dropdown-menu dropdown-menu-right pin-menu-right">';
            html +=             '<li><a class="item" href="javascript:void(0)"></li>';
            html +=             '<li><a class="dropdown-item pin-box" href="javascript:void(0)">'+pinText+'</a></li>';
            if(value.addedBySelf){
                html +=             '<li><a class="dropdown-item edit-link-box" data-type="edit" data-name="'+value.name+'" data-reftype="'+value.referenceType+'" data-inpname="'+value.inputName+'" data-refid="'+value.refId+'" data-link="'+value.link+'" href="javascript:void(0)">Edit</a></li>';
            };
            if(value.isShareEnabled) {
                html += '<li><a class="dropdown-item openShareWithModal" data-toggle="modal" data-refid="' + value.refId + '" sharedWithGroup="' + value.sharedWithGroup + '"   sharedWithUser="' + value.sharedWithUser + '" data-target="#sharedWithModal">Share</a></li>';
            };
                html +=             '<li><a class="dropdown-item ic-del deleteRecord"  isOwner="'+value.addedBySelf+'" data-refId="'+value.refId+'" data-toggle="modal" data-target="#deleteReferenceModal">Remove</a></li>';

            html +=         '</ul>';
            html +=     '</span>';
            html +=     '<a href="'+fileLink+'" class="lnk" target='+targetLink+'>'+encodeToHTML(value.inputName)+'</a>';
            html +=     '<div class="tooltip--multiline"> <p class="norap">'+encodeToHTML(value.inputName)+'</p><p class="norap"><strong>Modified By:</strong> '+value.modifiedBy+' </p>   <p><strong>Modified Date:</strong> '+value.timeStamp+'</p> </div>'
            html +=  '</div>';
        });

        $('#imageListId').html(html);
        initdraggableBox();
        manageDataOnScroll();
    }

    var resetArrowIcon= function(){
        $(".link-box-filter button").removeClass('active dt-ordering-desc');
    }

    var initdraggableBox = function(){
        $( "#imageListId" ).sortable({
            connectWith: "#imageListId",
            containment : "parent",
            scrollSpeed: 40,
            update: function(event, ui) {
                var isPinnedBox = ui.item.hasClass('pinned-box');
                var nextElement = ui.item.next();
                if(isPinnedBox){
                    if(nextElement.hasClass('pinned-box')){
                        getIdsLinks(true);
                    } else {
                        pinUnpinBox(ui.item);
                        getIdsLinks(false);
                    }

                } else if(!isPinnedBox){
                    if(nextElement.hasClass('pinned-box')){
                        pinUnpinBox(ui.item);
                        getIdsLinks(true);
                    } else {
                        getIdsLinks(false);
                    }
                }

            }//end update
        });
    }

    var getIdsLinks = function(pindrag) {
        var linkIds = [];
        if(pindrag){
            $('.pinned-box').each(function(index) {
                linkIds.push({'id':$(this).attr("data-id")});
            });
        } else {
            $('.listitemClass').not('.pinned-box').each(function(index) {
                linkIds.push({'id':$(this).attr("data-id")});
            });
        };

        var data = {
            refIds : JSON.stringify(linkIds),
            isPinned: pindrag
        }
        $.ajax({
            url: apiDragDropLink,
            type: "POST",
            contentType: 'application/json; charset=utf-8',
            data:JSON.stringify(data),
            dataType: "json",
            beforeSend: function () {
                // pvui.common.showPVLoader();
            },
        }).done(function (data) {
            resetArrowIcon();
            // drawLinkBox(data.aaData);
        }).fail(function (data) {
            // pvui.common.hidePVLoader();
        })
    }

    var manageAddLinkModal = function () {

        $(document).on('click', '#add-file-link', function () {
            var currRefId = $(this).data('ref-id');
            var refType= $("input[type='radio'][name='file-type']:checked").val();
            var fileTypeChecked = (refType == "Others")? true:false;
            var linkTypeTypeChecked = (refType == "Others")? false:true;
            var length = $("#attachment-name").val().length;
            //removed 255 length error as part of PVS-70960

            var formdata = new FormData();
            formdata.append("inputName", $("#attachment-name").val());
            // formdata.append("description", '');
            formdata.append("refrenceType", refType);
            formdata.append("fileTypeChecked",fileTypeChecked);
            formdata.append("linkTypeTypeChecked",linkTypeTypeChecked);
            if(linkTypeTypeChecked){
                formdata.append("referenceLink",$("#literature-file-path").val());
            } else {
                formdata.append("attachments",$("#refLinkModal .file-uploader .file")[0].files[0]);

            }

            if(currRefId){
                formdata.append("refrenceId",currRefId);
                formdata.append("isUpdateRequest",true);
            }

            $.ajax({
                url: 'dashboard/addRefrence',
                type: "POST",
                mimeType: "multipart/form-data",
                processData: false,
                contentType: false,
                data: formdata,
            }).done(function (data) {
                var response = JSON.parse(data);
                if(response.code === 500){
                    var $alert = $(".msgContainer").find('.alert');
                    $alert.find('.message').html(response.message);
                    $(".msgContainer").show();
                    $alert.alert();
                    $alert.fadeTo(5000, 1000).slideUp(1000, function () {
                        $alert.slideUp(1000);
                    });
                }
                if(response.status == true){
                    $("#refLinkModal").modal('hide'); //Added for PVS-57969
                }
                loadLinks();
            }).fail(function (data) {
                $.Notification.notify('error', 'top right', "Error", "Something went wrong, please try again", {autoHideDelay: 5000});
            });
            $("#refLinkModal .file-uploader .file")[0].value = '';
        });

        $(document).on('change', "#refLinkModal .file-uploader input[type='file']",  function () {
            var currentElement = $(this);
            if(currentElement.prop('files').length){
                $("#literature-file-path").val(currentElement.prop('files')[0].name);
                var filePath = $(this)[0].value;
                // Allowing file type
                var allowedExtensions = /(\.csv|\.pdf|\.xlx|\.doc|\.dot|\.docx|\.xls|\.xlsx|\.ppt|\.pptx|\.jpg|\.txt|\.png|\.gif|\.jpeg|\.vsd)$/i;

                if (!allowedExtensions.exec(filePath)) {
                    currentElement.closest('.form-group').addClass('has-error');
                    currentElement.closest('.form-group').find(".help-block").removeClass('hide');
                    $("#literature-file-path").val('');
                    $('#add-file-link').attr('disabled', true);
                    return false;
                } else{
                    currentElement.closest('.form-group').removeClass('has-error');
                    currentElement.closest('.form-group').find(".help-block").addClass('hide');
                }

                if(!$("#attachment-name").val()) {
                    $("#attachment-name").val(currentElement.prop('files')[0].name);
                }
            }
        });

        $(".file-uploader").find("#uploadAttachment").on('click', function () {
            $("#add-file-link").attr('disabled', false);
            $(".file-uploader").find("input[type='file']").trigger("click");
        });

        $('#refLinkModal input[type=radio][name=file-type]').on('change', function () {
            $("#literature-file-path").val('');
            $("#attachment-name").val('');
            if ($("#file-type-link").prop("checked")) {
                $("#refLinkModal .file-uploader .browse").prop('disabled', true);
                $("#literature-file-path").prop('disabled', false);
                $('#literature-file-path').attr('placeholder', 'Add reference link');
                $("#attachment-type-name").text('References');
            } else {
                $("#refLinkModal .file-uploader .browse").prop('disabled', false);
                $("#literature-file-path").prop('disabled', true);
                $('#literature-file-path').attr('placeholder', 'Attach a file');
                $(".file-uploader").show();
                $("#attachment-type-name").text('File Name');
            }
        });

        $(document).on("click", ".add-ref-link-modal, .edit-link-box", function () {
            var isEdit = $(this).data('type')=='edit';
            if(isEdit){
                $("#add-file-link").text("Update");
                var curr = $(this);
                var data = {
                    "refId": curr.data("refid"),
                    "name": curr.data("name"),
                    "refType":curr.data("reftype"),
                    "inputName":curr.data("inpname") ,
                    "link":curr.data("link"),
                };
                setLinkData(data);
            }
            $("#refLinkModal").modal('show');
            if(!isEdit){
                $("#attachment-type-name").text("File Name");
                $("#literature-file-path").attr("placeholder","Attach a file");
                $("#literature-file-path").prop('disabled', true);
                $("#refLinkModal .file-uploader .browse").prop('disabled', false);
                $(".form-group .error-message").addClass("hide");
                $("#add-file-link").attr("data-dismiss", "");
            }

            if(isEdit){
                if ($("#file-type-link").prop("checked")) {
                    $("#attachment-type-name").text('References');
                }
                else {
                    $("#attachment-type-name").text('File Name');
                }
            }
        });

        $(document).on("change",".modal-body .form-group",function(){
            if($("#literature-file-path").val()){
                $("#add-file-link").attr("data-dismiss", "modal");
            }
            else{
                $("#add-file-link").attr("data-dismiss", "");
            }
        })

        $(document).on('click','.pin-box',function () {
            var currBox = $(this).closest('.box-widget');
            var currRefId = currBox.data('id');
            var isPinned = currBox.hasClass("pinned-box")?false:true;
            var currPinEle = $(this);

            $.ajax({
                url: apiPinLink,
                type: "POST",
                data:{"refId":currRefId,"isPinned":isPinned},
            })
                .done(function (data) {
                    currBox.toggleClass("pinned-box");
                    var isPinned = currBox.hasClass("pinned-box");
                    if(isPinned){
                        currBox.prependTo("#imageListId");
                        currPinEle.text("Unpin");
                    }else{
                        var pos = $("#imageListId .pinned-box").length + 1;
                        $("#imageListId .box-widget:nth-child("+pos+")").after(currBox[0]); // currBox[0] represents the html of jquery element.
                        currPinEle.text("Pin");
                    }
                })
                .fail(function (data) {
                    // pvui.common.hidePVLoader();
                });

        });
        $('#refLinkModal').on('hidden.bs.modal', function (e) {
            $(this)
                .find("input[type='text'],textarea,select")
                .val('')
                .end();
            $("#refLinkModal .file-uploader input[type='file']").val('');
            $("#add-file-link").text("Add");
            $("#add-file-link").data("ref-id",null);
            $("input[type='radio'][name='file-type'][value='Others']").prop('checked', true);
        });
    };

    var pinUnpinBox = function (currBox) {
        var currRefId = currBox.data('id');
        var isPinned = currBox.hasClass("pinned-box") ? false : true;
        var currPinEle = currBox.find(".pin-box");
        currBox.toggleClass("pinned-box");
        var isPinned = currBox.hasClass("pinned-box");
        if (isPinned) {
            currPinEle.text("Unpin");
        } else {
            currPinEle.text("Pin");
        }
    };

    var setLinkData = function (data) {
        $("input[type='radio'][name='file-type'][value='"+data.refType+"']").prop('checked', true);
        $("#add-file-link").data('ref-id',data.refId);
        $("#attachment-name").val(data.inputName);
        var linkTypeTypeChecked = (data.refType == "Others")? false:true;
        if(linkTypeTypeChecked){
            $("#literature-file-path").val(data.link);
        } else {
            $("#literature-file-path").val(data.name);
        }
    };
    var manageLinkFilter = function () {
        $(document).on("click",".link-box-filter button", function () {
            var currBtn = $(this);
            var sortDir;
            var isActive = currBtn.hasClass('active');
            var sortType = currBtn.data('type');
            if(isActive){
                if(currBtn.hasClass('dt-ordering-desc')){
                    sortDir='asc';
                } else{
                    sortDir='desc';
                }
                currBtn.toggleClass('dt-ordering-desc');

            } else{
                $(".link-box-filter button").removeClass('active dt-ordering-desc');
                currBtn.addClass('active')
            }
            var sortData = {
                "len": reqLen||50,
                "stype": sortType,
                "dir": sortDir || 'asc'
            }
            loadLinks(sortData);
        });

        $(document).on("keyup",".link-box-filter input[type='search']", function () {
            if( $(this).val().length >= 1 || !$(this).val().length){
                var sortData = {
                    "qs": $(this).val()
                }
                loadLinks(sortData);
            };
        });
        $('#custom-search-links-wd').on('keyup', function (){
            if( $(this).val().length >= 1 || !$(this).val().length){
                var sortData = {
                    "qs": $(this).val()
                }
                loadLinks(sortData);
            };
        })
    };

    var manageDataOnScroll = function () {
        $("#imageListId").on('scroll',function() {
            var scrollableHeight = Math.round(document.getElementById("imageListId").scrollHeight);
            var scrolled = Math.round($(this).scrollTop());
            var isScrollable = reqLen? (totalLinkCount-reqLen > 0): totalLinkCount > 50;
            if ($(window).data('ajax_in_progress') === true)
                return;

            var isSorted= $(".link-box-filter button").hasClass("active");
            if(isScrollable && scrolled > lastScrollTop && scrollableHeight-scrolled < 300  ) {
                pageNum++;
                reqLen = reqLen?reqLen+50:75;
                if(isSorted){
                    var sortdata= {
                        "stype":$(".link-box-filter button.active").data("type"),
                        "dir":$(".link-box-filter button.active").hasClass("dt-ordering-desc")?"desc":"asc",
                        "len": reqLen,
                        "scrolling": true
                    };
                } else{
                    var sortdata= {
                        "len": reqLen,
                        "scrolling": true
                    }
                }


                $(window).data('ajax_in_progress', true);
                setTimeout(function(){loadLinks(sortdata)}, 1500);

            };
            lastScrollTop = scrolled;
        });
    };

    var manageScrollbar = function () {
        var boxTitleMenu;
        $(document).on("mouseover",".box-widget>a", function () {
            boxTitleMenu = $(this).next('.tooltip--multiline');
            var eOffset = $(this).offset();
            $('body').append(boxTitleMenu.css({
                'display': 'block',
                'top': Math.round(eOffset.top) - Math.round($(this).outerHeight()),
                'left': Math.round(eOffset.left)-20,
                'max-width': '200px',
                'height':boxTitleMenu.outerHeight()
            }).detach());
        }).on("mouseout",".box-widget>a", function () {
            $(this).parent().append(boxTitleMenu.detach());
            boxTitleMenu.hide();
        });
    };

    var init = function () {
        reqLen=0;
        resetArrowIcon();
        loadLinks();
    };

    return {
        init: init,
        manageAddLinkModal:manageAddLinkModal,
        manageLinkFilter:manageLinkFilter,
        manageScrollbar:manageScrollbar
    }
})();

$(function () {
    signal.managelinkwidget.init();
    signal.managelinkwidget.manageAddLinkModal();
    signal.managelinkwidget.manageLinkFilter();
    signal.managelinkwidget.manageScrollbar();
    $(document).on("click", ".refresh-refrences-link", function () {
        signal.managelinkwidget.init();
        $(".link-box-filter input").val("");
    });
})
