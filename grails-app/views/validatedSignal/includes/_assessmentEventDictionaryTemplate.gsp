<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<div class="row" xmlns="http://www.w3.org/1999/html">
    <div class="col-xs-2 padding-md-bottom">
        <label><g:message code="app.eventDictionary.eventGroup"/></label>
    </div>
    <div class="col-xs-10 padding-md-bottom">
        <select id="eventGroupSelectAssessment" class="eventGroupAssessment form-control" style="width: 100%"></select>
    </div>
</div>

<div class="row">
    <div class="col-xs-2 padding-md-bottom">
        <label><g:message code="app.eventDictionary.smq"/></label>
    </div>
    <div class="col-xs-10 padding-md-bottom">
        <g:if test="${PVDictionaryConfig.select2v4}">
            <select id="eventSmqSelectAssessment" class="eventSmqAssessment form-control" style="width: 100%"></select>
        </g:if><g:else>
        <input id="eventSmqSelectAssessment" class="eventSmqAssessment form-control" style="width: 100%"/>
    </g:else>
    </div>
</div>

<div class="row">
    <div class="col-xs-5 padding-md-bottom">
        <div class="checkbox checkbox-primary">
            <g:checkBox id="event_exactSearch_assessment" name="event_exactSearch"/>
            <label for="event_exactSearch">
                <g:message code="app.label.dictionary.exact.search"/>
            </label>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.soc"/><i tabindex="0"
                                                             class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal"
                                                             data-target="#copyAndPasteDicModal"></i></label>
        <input level="1" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.hlgt"/><i tabindex="0"
                                                              class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal"
                                                              data-target="#copyAndPasteDicModal"></i></label>
        <input level="2" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.hlt"/><i tabindex="0"
                                                             class="fa fa-pencil-square-o copy-n-paste modal-link hidden" data-toggle="modal"
                                                             data-target="#copyAndPasteDicModal"></i></label>
        <input level="3" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.pt" /><i tabindex="0"
                                                             class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal"
                                                             data-target="#copyAndPasteDicModal"></i></label>
        <input level="4" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.llt"/><i tabindex="0"
                                                             class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal"
                                                             data-target="#copyAndPasteDicModal"></i></label>
        <input level="5" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.eventDictionary.synonyms" /><i tabindex="0"
                                                                   class="fa fa-pencil-square-o copy-n-paste modal-link hidden" data-toggle="modal"
                                                                   data-target="#copyAndPasteDicModal"></i></label>
        <input level="6" class="searchEvents form-control" type="text">

    </div>
</div>

<div class="row">
    <div class="col-xs-12">
        %{--<button class="btn btn-default" is="column-view-back" for="columnView">back</button>--}%
        <column-view style="height: 200px;" id="columnView" ondata="source" path="0" dictionary_type="event" columns_name="${column_names.join(',')}" ></column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedEventDictionaryValueAssessment">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>
    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-2">
                <div style="width: 100px">
                    <label><g:message code="app.eventDictionary.eventGroup"/></label>
                </div>
            </div>
            <div class="col-xs-10 level-event-group-assessment ulEventGroupAssessment"></div>
        </div>
        <div class="row">
            <div class="col-xs-2">
                <div style="width: 100px">
                    <label><g:message code="app.eventDictionary.smqb" /></label>
                </div>
            </div>
            <div class="col-xs-10 eventDictionaryValueAssessment level7 ulSmqb"></div>
        </div>
        <div class="row">
            <div class="col-xs-2">
                <div style="width: 100px">
                    <label><g:message code="app.eventDictionary.smqn" /></label>
                </div>
            </div>
            <div class="col-xs-10 eventDictionaryValueAssessment level8 ulSmqn"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.soc"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level1 ulSOC"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.hlgt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level2 ulHLGT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.hlt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level3 ulHLT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.pt" /></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level4 ulPT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.llt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level5 ulLLT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.eventDictionary.synonyms" /></label>
            </div>
            <div class="col-xs-11 eventDictionaryValueAssessment level6 ulSynonyms"></div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-xs-12">
    </div>
</div>

<asset:javascript src="app/pvs/validated_signal/assessmentEventGroup.js"/>
<style>
.eventDictionaryColWidth {
    width: calc(100%/${column_names.size()});
}
</style>