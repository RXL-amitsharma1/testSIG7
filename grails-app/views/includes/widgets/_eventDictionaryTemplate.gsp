<g:if test="${sMQList}">
    <div class="row m-b-10 m-b-15">
        <label class="pull-left" style="width:40px; margin-left: 5px;">SMQ</label>

        <div class="pull-left" style="width:calc(100% - 50px)">
            <g:select id="smqValues" name="smqValues" optionValue="name" optionKey="code"
                      from="${sMQList}" multiple="true"
                      class="form-control smqValues select2"/>
        </div>
    </div>
</g:if>
<div class="row m-b-15" id="eventSelectionDictionary">
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

<div class="row m-b-15">
    <div class="col-xs-12 host m-t-5">
            <column-view style="height: 200px;" id="columnView1" ondata="source" path="0" dic="product" dictionary_type="event" tabindex="0" role="tree">
                <div id="carriage" class="style-scope column-view">
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="SOC"></ul>
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="HLGT"></ul>
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="HLT"></ul>
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="PT"></ul>
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="LLT"></ul>
                    <ul class="eventDictionaryColWidth dicUlFormat" dictionaryLevel="Synonyms"></ul>
                </div>
            </column-view>
        %{--<button class="btn btn-default" is="column-view-back" for="columnView">back</button>--}%
        %{--<column-view style="height: 200px;" id="columnView" ondata="source" path="0" dictionaryType="event"></column-view>--}%
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedEventDictionaryValue m-b-15">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>
    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-1">
                <label><g:message code="app.reportField.labDataSmq"/></label>
            </div>
            <div class="col-xs-11 eventDictionaryValue level7 ulSMQ"></div>
        </div>
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
<div class="row m-b-15">
    <div class="col-xs-12">
        %{--<input type="button" class="btn btn-default" value="Clear">--}%
    </div>
</div>
