<div class="row">
    <div class="col-md-4">

        <div class="rxmain-container">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Reaction Groups
                    </label>
                </div>
                <div class="rxmain-container-content">
                    <div class="row">
                        <input type="button" class="btn btn-default" id="deselectAllReactionGroups" value="Deselect All">
                        <input type="button" class="btn btn-primary" id="selectAllReactionGroups" value="Select All">
                        <i id="refreshCasesByReactionGroup" class="fa fa-refresh" aria-hidden="true"
                           style="float: right; margin-top: 8px; cursor:pointer"></i>
                    </div>
                </div>

                <div class="rxmain-container-content" id="socListForCasesByReactionGroup">
                    <g:each in="${socList}" var="soc">
                        <div class="row">
                            <input type="checkbox" name="soc" value="${soc.id}"
                                   checked="checked">&nbsp;&nbsp;&nbsp;<label>${soc.name}</label>
                        </div>
                    </g:each>
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
                        <div id="age-and-gender-group-chart"></div>
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
                        <div id="reporter-group-chart"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="rxmain-container">
            <div class="rxmain-container-inner">

                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Region (EEA/Non-EEA)
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <div class="row">
                        <div id="geographic-region-chart"></div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
