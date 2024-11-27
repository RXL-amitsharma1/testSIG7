<div class="row">
    <div class="col-xs-4">
        <label><g:message code="app.studyDictionary.studyNumber.label"/></label>
        <input level="1" class="searchStudies form-control" type="text">

    </div>
    <div class="col-xs-4">
        <label><g:message code="app.studyDictionary.protocolNumber.label"/></label>
        <input level="2" class="searchStudies form-control" type="text">

    </div>
    <div class="col-xs-4">
        <label><g:message code="app.label.studyDictionary.center"/></label>
        <input level="3" class="searchStudies form-control" type="text">

    </div>

</div>

<div class="row">
    <div class="col-xs-12 host m-t-5">
        <column-view style="height: 200px;" id="columnView1" ondata="source" path="0" dic="product" dictionary_type="product" tabindex="0" role="tree">
            <div id="carriage" class="style-scope column-view">
                <ul class="studyDictionaryColWidth dicUlFormat" dictionarylevel="Study Number"></ul>
                <ul class="studyDictionaryColWidth dicUlFormat" dictionarylevel="Project Number"></ul>
                <ul class="studyDictionaryColWidth dicUlFormat" dictionarylevel="Center"></ul>
            </div>
        </column-view>
    </div>
</div>

%{--Show selected value as text--}%
<div class="row selectedStudyDictionaryValue">
    <div class="col-xs-1">
        <label><g:message code="app.label.selected"/>:</label>
    </div>
    <div class="col-xs-10">
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.studyDictionary.studyNumber.label"/></label>
            </div>
            <div class="col-xs-9 studyDictionaryValue level1"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.studyDictionary.protocolNumber.label"/></label>
            </div>
            <div class="col-xs-9 studyDictionaryValue level2"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <label><g:message code="app.label.studyDictionary.center"/></label>
            </div>
            <div class="col-xs-9 studyDictionaryValue level3"></div>
        </div>

    </div>
</div>
<div class="row">
    <div class="col-xs-12">
        %{--<input type="button" class="btn btn-default" value="Clear">--}%
    </div>
</div>