<div class="pv-tab reactionGroupDetails">
    <!-- Nav tabs -->
    <ul id="individualCasesByReactionTabs" class="validation-tab m-b-5 p-0" role="tablist">
        <li role="presentation" class="active">
            <a href="#reactionGroupByAge" aria-controls="details" role="tab"
               data-toggle="tab">Age Group</a>
        </li>
        <li role="presentation">
            <a href="#reactionGroupByGender" aria-controls="details" role="tab"
               data-toggle="tab">Gender</a>
        </li>
        <li role="presentation">
            <a href="#reactionGroupByReporter" aria-controls="details" role="tab"
               data-toggle="tab">Reporter Group</a>
        </li>
        <li role="presentation">
            <a href="#reactionGroupByRegion" aria-controls="details" role="tab"
               data-toggle="tab">Region (EEA/Non-EEA)</a>
        </li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="reactionGroupByAge">
            <g:render template="includes/reactionGroupByAge" model="[socList: socList]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="reactionGroupByGender">
            <g:render template="includes/reactionGroupByGender" model="[socList: socList]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="reactionGroupByReporter">
            <g:render template="includes/reactionGroupByReporter" model="[socList: socList]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="reactionGroupByRegion">
            <g:render template="includes/reactionGroupByRegion" model="[socList: socList]"/>
        </div>
    </div>
</div>
