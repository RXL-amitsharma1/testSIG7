$(function () {
    $("input.searchProducts, input.searchStudies, input.searchEvents").on("focusin", function () {
        showPencilIcon($(this));
    }).on("focusout", function (event) {
        hidePencilIcon($(this));
    });
    $('#copyAndPasteDicModal').on('show.bs.modal', function () {
        $("#productModalAssessment").css('z-index', 1000);
    });
    $('#copyAndPasteDicModal').on('hidden.bs.modal', function () {
        $(".modal-backdrop").remove()
        $("#productModalAssessment").css('z-index', 1050);
    });
    function showPencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").css("opacity", "1.0");
    }
    function hidePencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").css("opacity", "0.01");
    }
    $("#dataSourcesProductDictAssessment").select2();

    $("#saveProductGroupAssessment").on('click', function () {
        var productGroupModal = $("#productGroupModalDictAssessment");
        var isFaersPresent = false;
        var isVaersPresent = false;
        var isVigibasePresent = false;
        var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var valueFaers = dictionaryObj.getValuesFaers();
        var valueVaers = dictionaryObj.getValuesVaers();
        var valueVigibase = dictionaryObj.getValuesVigibase();
        var productJsonSize = Object.values(dictionaryObj.getValuesPva()).map(arr => arr.length);
        var totalProductSelected = productJsonSize.reduce((acc, curr) => acc + curr, 0);
        if(totalProductSelected >0){
            dictionaryObj.getValuesPva().isMultiIngredient = $("#productGroupIsMultiIngredientAssessment").val();
        }
        var drugRecNumExist =  dictionaryObj.getValuesPva().hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL) && dictionaryObj.getValuesPva()[drugRecNumLevel].length > 0
        var object = {
            'pva': dictionaryObj.getValuesPva(),
            'faers': dictionaryObj.getValuesFaers(),
            'eudra': dictionaryObj.getValuesEudra(),
            'vaers': dictionaryObj.getValuesVaers(),
            'vigibase': dictionaryObj.getValuesVigibase()
        };
        var params = {
            id: $("#productGroupIdAssessment").val(),
            groupName: $("#productGroupNameAssessment").val(),
            description: $("#descriptionProductGroupAssessment").val(),
            sharedWith: $("#shareWithProductGroupAssessment").val() != null ? $("#shareWithProductGroupAssessment").val().join(';') : "",
            data: JSON.stringify(object),
            isMultiIngredient: $("#productGroupIsMultiIngredientAssessment").val(),
            includeWHODrugs: $("#productGroupIsWhoDrugAssessment").val() && drugRecNumExist
        };
        if ($("#productGroupDictLabelAssessment").text() == "Update Product Group ") {
            if(!dictionaryObj.getSelectedDicGroups().length) {
                params.copyGroups = dictionaryObj.getValuesDicGroup()[0].id
            }
        } else {
            //copy scenario
            var productGrpList = [];
            if (dictionaryObj.getValuesDicGroup().length) {
                $.each(dictionaryObj.getValuesDicGroup(), function (k, v) {
                    if (dictionaryObj.getSelectedDicGroups().length) {
                        if (dictionaryObj.isShowEnabled(v.id)) {
                            productGrpList.push(v.id)
                        }
                    } else {
                        productGrpList.push(v.id)
                    }
                });
                params.copyGroups = productGrpList.join(',')
            }
        }
        if($("#productGroupIdAssessment").val() && typeof isShowAllClicked != 'undefined' && !isShowAllClicked){
            updatingProductsInProductGroup();
        } else {
            addingProductsInProductGroup();
        }

        function updatingProductsInProductGroup(){
            $.ajax({
                type: "POST",
                url: showAllProductsUrl,
                data: {id: $("#productGroupIdAssessment").val() },
                dataType: "json",
            })
                .done(function (res) {
                    var obj = JSON.parse(res.result.data);
                    if (obj.faers != undefined && Object.keys(obj.faers).length > 0) {
                        var productGroupValuesFaers = obj.faers;
                        $.each(productGroupValuesFaers, function (key, value) {
                            if (value.length != 0) {
                                isFaersPresent = true;
                            }
                        });
                    }
                    if (obj.vaers != undefined && Object.keys(obj.vaers).length > 0) {
                        var productGroupValuesVaers = obj.vaers;
                        $.each(productGroupValuesVaers, function (key, value) {
                            if (value.length != 0) {
                                isVaersPresent = true;
                            }
                        });
                    }
                    if (obj.vigibase != undefined && Object.keys(obj.vigibase).length > 0) {
                        var productGroupValuesVigibase = obj.vigibase;
                        $.each(productGroupValuesVigibase, function (key, value) {
                            if (value.length != 0) {
                                isVigibasePresent = true;
                            }
                        });
                    }
                    addingProductsInProductGroup();
                });
        }

        function addingProductsInProductGroup() {
            if ($("#productGroupNameAssessment").val() == "" || $("#productGroupNameAssessment").val().trim() == "" || !$("#shareWithProductGroupAssessment").val()) {
                showErrorMessage("Please fill the required fields.", $('#productGroupModalDictAssessment .modal-body'));
                delete dictionaryObj.getValuesPva().isMultiIngredient
            } else if (isFaersAndVaersData()) {
                showErrorMessage("Creation or updation of a product group is not allowed with the combination of VAERS and FAERS products.",
                    $('#productGroupModalDictAssessment .modal-body'));
                delete dictionaryObj.getValuesPva().isMultiIngredient
            } else {
                productGroupModal.find('.btn-primary').each(function () {
                    $(this).prop('disabled', true);
                });
                $.ajax({
                    type: "POST",
                    url: saveProductGroupUrl,
                    data: params,
                    dataType: "json",
                })
                    .done(function (result) {
                        if (result.status == true) {
                            var successMsg = '<div class="alert alert-success alert-dismissible" role="alert"> ';
                            successMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                            successMsg += '<span aria-hidden="true">&times;</span> ';
                            successMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                            successMsg += '</button> ' + result.message;
                            successMsg += '</div>';
                            productGroupModal.modal("hide");
                            $('#productModalAssessment .modal-body').prepend(successMsg);
                            $("#productGroupSelectAssessment").val('').trigger('change') ;
                            clearDictionaryModal(PRODUCT_DICTIONARY);
                            $('#mainContent').trigger("columnView.reset." + PRODUCT_DICTIONARY);
                            dictionaryObj.addToValuesDicGroup({
                                name: result.name,
                                id: result.groupId,
                                isMultiIngredient: result.isMultiIngredient,
                                includeWHODrugs: result.includeWHODrugs
                            });
                            dictionaryObj.isCloseDictionary = true;
                            dictionaryObj.setProductGroupUpdateText(result);
                            enableDisableProductGroupButtons(dictionaryObj.getValues());
                        } else {
                            var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
                            errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                            errorMsg += '<span aria-hidden="true">&times;</span> ';
                            errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                            errorMsg += '</button> ' + result.message;
                            errorMsg += '</div>';
                            productGroupModal.modal("hide");
                            $('#productModalAssessment .modal-body').prepend(errorMsg);
                        }

                        setTimeout(function () {
                            addHideClass($(".alert-success"));
                            addHideClass($(".alert-danger"));
                        }, 5000);
                    })
                    .fail(function (err) {
                        alert(err);
                        productGroupModal.modal("hide");

                    })
                    .always(function () {
                        productGroupModal.find('.btn-primary').each(function () {
                            $(this).prop('disabled', false);
                        });
                    });
            }
        }

        function isFaersAndVaersData(){
            var level = 4; // As per the present configuration.
            for(var i = 0; i < level; i++){
                if(typeof valueFaers[i] != 'undefined' && valueFaers[i].length > 0){
                    isFaersPresent = isFaersPresent || true;
                }
                if(typeof valueVaers[i] != 'undefined' && valueVaers[i].length > 0){
                    isVaersPresent = isVaersPresent || true;
                }
            }
            return isFaersPresent && isVaersPresent;
        }
        function isVaersAndVigibaseData(){
            var level = 4; // As per the present configuration.
            for(var i = 0; i < level; i++){
                if(typeof valueVaers[i] != 'undefined' && valueVaers[i].length > 0){
                    isVaersPresent = isVaersPresent || true;
                }
                if(typeof valueVigibase[i] != 'undefined' && valueVigibase[i].length > 0){
                    isVigibasePresent = isVigibasePresent || true;
                }
            }
            return isVaersPresent && isVigibasePresent;
        }
        resetProductGroupModal();
    });

    $("#deleteProductGroupAssessment").on('click', function () {
        var modal = $("#deleteModalAssessment");
        modal.modal("show");
        $('#deleteDlgErrorDivAssessment').hide();
        modal.find('#nameToDelete').text('Are you sure you want to delete the product group');
    });

    $("#deleteProdGrpButtonAssessment").on("click", function () {
        var productGroupModal = $("#productGroupModalDictAssessment");
        var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var modal = $("#deleteModalAssessment");
        var params = {
            groupName: $("#productGroupNameAssessment").val(),
            id : $("#productGroupIdAssessment").val()
        };

        $.ajax({
            type: "POST",
            url: deleteProductGroupUrl,
            data: params,
            dataType: "json",
        })
            .done(function (result) {
                console.log("result", result);
                if (result.status == true) {
                    var successMsg = '<div class="alert alert-success alert-dismissible" role="alert"> ';
                    successMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                    successMsg += '<span aria-hidden="true">&times;</span> ';
                    successMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                    successMsg += '</button> ' + result.message;
                    successMsg += '</div>';
                    modal.modal("hide");
                    productGroupModal.modal("hide");
                    $('#productModalAssessment .modal-body').prepend(successMsg);
                    $("#productGroupSelectAssessment").val('').trigger('change');
                    clearDictionaryModal(PRODUCT_DICTIONARY);
                    enableDisableProductGroupButtons(dictionaryObj.getValues());
                } else {
                    var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
                    errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
                    errorMsg += '<span aria-hidden="true">&times;</span> ';
                    errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
                    errorMsg += '</button> ' + result.message;
                    errorMsg += '</div>';
                    productGroupModal.modal("hide");
                    modal.modal("hide");
                    $('#productModalAssessment .modal-body').prepend(errorMsg);
                }
                setTimeout(function () {
                    addHideClass($(".alert-success"));
                    addHideClass($(".alert-danger"));
                }, 5000);
            })
            .fail(function (err) {
                alert(err);
                modal.modal("hide");
                productGroupModal.modal("hide");
            });
        resetProductGroupModal();
    });

    var addHideClass = function(row) {
        row.remove();
    };

    $(".closeProductGroupModalAssessment").on('click', function () {
        $("#productGroupModalDictAssessment").modal("hide");
        resetProductGroupModal();
    });

    $(".closeDeleteModalAssessment").on('click', function () {
        $("#deleteModalAssessment").modal("hide");
    });

    var resetProductGroupModal = function () {
        $('#productGroupModalDictAssessment').on('hidden.bs.modal', function (e) {
            clear_messages();
            $(this).find("input[type=text]").val('').end();
            $(this).find("#descriptionProductGroupAssessment").val('');
            $(this).find("#shareWithProductGroupAssessment").val(null).trigger('change');
            $(this).find("#productGroupIdAssessment").val('');
            $(this).find("#productGroupNameAssessment").val('');
            $(this).find("#descriptionProductGroupAssessment").val('');
            $(this).find("#productGroupIsMultiIngredientAssessment").val('');
            $(this).find("#productGroupIsWhoDrugAssessment").val('');
        });
    };

    var clear_messages = function () {
        $("#productGroupModalDictAssessment").find('.alert').hide();
    };

    function showErrorMessage(message, modal) {
        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
        errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
        errorMsg += '<span aria-hidden="true">&times;</span> ';
        errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
        errorMsg += '</button> ' + message;
        errorMsg += '</div>';
        modal.prepend(errorMsg);
        setTimeout(function () {
            addHideClass($(".alert-danger"));
        }, 5000);
    }

});