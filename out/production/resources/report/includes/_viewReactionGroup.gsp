<div class="row">
    <div class="col-md-4">

        <div class="rxmain-container">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Selected Reaction Groups
                    </label>
                </div>

                <div class="rxmain-container-content" id="socListForCasesByReactionGroup">
                    <g:each in="${socList}" var="soc">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>
                                        ${soc}
                                    </label>
                                </div>
                            </div>
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
