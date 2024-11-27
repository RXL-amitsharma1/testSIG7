<div class="row">
    <div class="col-md-4">

        <div class="rxmain-container">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Reactions
                    </label>
                </div>

                <div class="rxmain-container-content">

                    <div class="row">
                        <label for="groups-for-reaction">Reaction Groups</label>
                        <g:select id="groups-for-reaction" name="productName" from="${socList}" optionKey="id"
                                  optionValue="name" class="form-control"/>
                    </div>
                    <br/>

                    <div class="row">
                        <input type="button" class="btn btn-default" id="deselectAllReactions" value="Deselect All">
                        <input type="button" class="btn btn-primary" id="selectAllReactions" value="Select All">
                        <i id="refreshCasesByReaction" class="fa fa-refresh fa-disabled" aria-hidden="true"
                           style="float: right; margin-top: 8px; cursor:pointer"></i>
                    </div>

                    <div class="row ">
                        <label class="reactionRowLabel">Reactions</label>

                        <div class="reactionRow" style="max-height:1250px; overflow-y:auto; border-color: black"
                             id="socListForCasesByReaction">
                            <g:each in="${ptList}" var="pt">
                                <div><input type="checkbox" name="soc" value="${pt.id}">&nbsp;&nbsp;&nbsp;<label>${pt.name}</label>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div class="col-md-8">

        <div class="rxmain-container">
            <div class="rxmain-container-inner">

                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Age Group and Gender
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <div class="row">
                        <div id="reaction-chart"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="rxmain-container">
            <div class="rxmain-container-inner">

                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Reporter Group
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <div class="row">
                        <div id="reporter-reaction-chart"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="rxmain-container">
            <div class="rxmain-container-inner">

                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Outcome
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <div class="row">
                        <div id="reaction-outcome-chart"></div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
