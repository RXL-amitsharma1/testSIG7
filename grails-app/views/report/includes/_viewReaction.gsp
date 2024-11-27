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
                        <label>Selected Reaction Group:</label>

                        <div class="col-md-12">
                            <div class="form-group">
                                <label>
                                    ${selectedSOCForReaction}
                                </label>
                            </div>
                        </div>
                    </div>
                    <br/>

                    <div class="row ">
                        <label class="reactionRowLabel">Selected Reactions:</label>

                        <div class="reactionRow" style="max-height:1250px; overflow-y:auto; border-color: black"
                             id="socListForCasesByReaction">
                            <g:each in="${ptList}" var="pt">
                                <div class="col-md-12">
                                    <div class="form-group">
                                        <label>
                                            ${pt}
                                        </label>
                                    </div>
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
