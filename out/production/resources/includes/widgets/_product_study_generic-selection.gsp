<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.signal.SingleCaseAlert" %>
<style>
.showDictionarySelection {
    margin-bottom: 7px;
}
.suspect-font {
    font-weight: 400 !important;
}
</style>
%{--Product Selection/Study Selection--}%
<g:if test="${isAdhocAlert}">
    <div class="col-md-4">
</g:if>
<g:else>
    <div class="col-md-3">
</g:else>
<div class="${hasErrors(bean: theInstance, field: 'productSelection', 'has-error')}">

    <label class="radio-inline labelBold m-b-5">
        <g:if test="${!editMode || checked == 'product'}">
            <input type="radio" name="optradio" class="productRadio" checked/>
        </g:if>
        <g:else>
            <input type="radio" name="optradio" class="productRadio"/>
        </g:else>
        <g:message code="app.label.product.label"/>
    </label>

    <g:if test="${!(theInstance instanceof com.rxlogix.config.LiteratureConfiguration)}">
        %{--<label class="radio-inline labelBold ${showHideProductGroupVal}">--}%
            %{--<g:if test="${checked == 'productGroup'}">--}%
                %{--<input type="radio" name="optradio" class="productGroupRadio" checked/>--}%
            %{--</g:if>--}%
            %{--<g:else>--}%
                %{--<input type="radio" name="optradio" class="productGroupRadio"/>--}%
            %{--</g:else>--}%
            %{--<g:message code="app.label.product.group.label"/>--}%
        %{--</label>--}%

        <label class="m-b-5 radio-inline labelBold ${showHideGenericVal}">
            <g:if test="${checked == 'generic'}">
                <input type="radio" name="optradio" class="genericRadio" checked/>
            </g:if>
            <g:else>
                <input type="radio" name="optradio" class="genericRadio"/>
            </g:else>
            <g:message code="generic.names.selection.label"/>
        </label>

        <g:if test="${grails.util.Holders.config.study.dictionary.enabled}">
            <label class="m-b-5 radio-inline labelBold ${showHideStudyVal}">
                <g:if test="${theInstance?.studySelection != null}">
                    <input type="radio" name="optradio" class="studyRadio" checked>
                </g:if>
                <g:else>
                    <input type="radio" name="optradio" class="studyRadio"/>
                </g:else>
                <g:message code="app.label.study.label"/>
            </label>
        </g:if>
        <label><span class="required-indicator">*</span></label>
    </g:if>
</div>

<div class="row"/>

<div class="col-md-12">
    <div class="wrapper">
        <div id="showProductSelection" class="showDictionarySelection"></div>

        <div class="iconSearch">
            <a data-toggle="modal" data-target="#productModal" tabindex="0" data-toggle="tooltip"
               title="Search product" data-is-quantitative="${isQuant ? true : false}" data-allowed-sources-product="pva,faers,eudra,vaers,vigibase" accesskey="p"><i class="fa fa-search"></i></a>
        </div>
    </div>

    <div class="wrapper" hidden="hidden">
        <div id="showProductGroupSelection" class="showDictionarySelection"></div>

        <div class="iconSearch">
            <i class="fa fa-search" data-toggle="modal" tabindex="0" data-target="#productGroupModal"></i>
        </div>
    </div>

    <div class="wrapper" hidden="hidden">
        <div id="showStudySelection" class="showDictionarySelection"></div>

        <div class="iconSearch">
            <a data-toggle="modal" data-target="#studyModal" tabindex="0" data-toggle="tooltip" title="Search study"  accesskey="{"><i
                    class="fa fa-search"></i></a>
        </div>

    </div>

    <g:if test="${(theInstance instanceof Configuration) && theInstance.type == "Single Case Alert"}">
        <div class="checkbox checkbox-primary">
            <g:checkBox id="limitToSuspectProduct" name="suspectProduct"
                        value="${theInstance?.suspectProduct}"
                        checked="${theInstance?.suspectProduct}"/>
            <label class="suspect-font" for="limitToSuspectProduct">
                <g:message code="app.label.configuration.suspectProduct"/>
            </label>
        </div>
        <g:hiddenField name="isSuspectProduct" id="limitTosuspectProduct" value="${theInstance?.suspectProduct}"/>
    </g:if>

    <div class="wrapper" hidden="hidden">
        <div id="showGenericSelection" class="showDictionarySelection"></div>

        <div class="iconSearch">
            <a data-toggle="modal" data-target="#genericModal" tabindex="0" data-toggle="tooltip"
               title="Search Generic" accesskey=":"><i class="fa fa-search"></i></a>
        </div>
    </div>
</div>
</div>

<g:hiddenField name="productSelection" value="${theInstance?.productSelection}"/>
<g:if test="${!(theInstance instanceof com.rxlogix.config.LiteratureConfiguration)}">
    <g:hiddenField name="studySelection" value="${theInstance?.studySelection}"/>
</g:if>
<g:if test="${!(theInstance instanceof com.rxlogix.config.LiteratureConfiguration) && !(theInstance instanceof com.rxlogix.signal.AdHocAlert) }">
    <g:hiddenField name="isMultiIngredient" value="${theInstance?.isMultiIngredient}"/>
</g:if>

<div class="row" hidden="hidden">
    <div class="col-md-12">
        <!-- TODO: For debugging use only; type="hidden" for this input field after we are done needing to see it -->
        <input name="JSONExpressionValues" id="JSONExpressionValues" value=""/>
        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message code="prettify.my.json.here"/></a>
    </div>
</div>

</div>

<div class="modal fade" id="genericModal" tabindex="-1" role="dialog" aria-labelledby="genericDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="genericDictionaryLabel"><g:message
                        code="generic.names.selection.label"/></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-md-12">
                        <select id="js-data-generics" class="form-control" name="genericNames" multiple="multiple">
                        </select>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary addGenericValues" data-dismiss="modal"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default clearGenericValues"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-default closeGenericValues" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>

            </div>
        </div>
    </div>
</div>
<g:javascript>
    $(document).ready(function () {
        if (typeof theInstance !='undefined' && (theInstance instanceof com.rxlogix.config.Configuration) && theInstance.type == "Single Case Alert" && theInstance.suspectProduct) {
            $("#limitToSuspectProduct").prop("checked")
        }
        if($("#limitToSuspectProduct").val() == "on"){
            $("#limitToSuspectProduct").val(true);
        }
        else {
            $("#limitToSuspectProduct").val(false);
        }
        $("#limitToSuspectProduct").click(function () {
            if ($("#limitToSuspectProduct").prop("checked")) {
                $("#limitTosuspectProduct").val(true);
            } else {
                $("#limitTosuspectProduct").val(false);
            }
        });
        if(document.getElementsByClassName("studyRadio").length >0 && document.getElementsByClassName("studyRadio")[0].checked == true) {
            $("#limitToSuspectProduct").prop("disabled", true);
            $("#limitToSuspectProduct").prop("checked", false);
            $("#limitTosuspectProduct").val(false);
        }
    })
</g:javascript>
