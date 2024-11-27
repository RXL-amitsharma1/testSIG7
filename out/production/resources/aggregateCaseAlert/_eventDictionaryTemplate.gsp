<div class="row">
    <div class="col-xs-2">
        <label><g:message code="app.reportField.labDataSoc"/></label>
        <input level="1" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.reportField.labDataHlgt"/></label>
        <input level="2" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.reportField.labDataHlt"/></label>
        <input level="3" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="eventDictionary.pt" /></label>
        <input level="4" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="app.reportField.labDataLlt"/></label>
        <input level="5" class="searchEvents form-control" type="text">

    </div>
    <div class="col-xs-2">
        <label><g:message code="eventDictionary.synonyms" /></label>
        <input level="6" class="searchEvents form-control" type="text">

    </div>
</div>

<div class="row">
    <div class="col-xs-12">
        %{--<button class="btn btn-default" is="column-view-back" for="columnView">back</button>--}%
        <column-view style="height: 200px;" id="columnView" ondata="source" path="0" dictionaryType="event"></column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedEventDictionaryValue">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>
    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.reportField.labDataSoc"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level1 ulSOC"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.reportField.labDataHlgt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level2 ulHLGT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.reportField.labDataHlt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level3 ulHLT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="eventDictionary.pt" /></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level4 ulPT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.reportField.labDataLlt"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level5 ulLLT"></div>
        </div>
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="eventDictionary.synonyms" /></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level6 ulSynonyms"></div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-xs-12">
        %{--<input type="button" class="btn btn-default" value="Clear">--}%
    </div>
</div>