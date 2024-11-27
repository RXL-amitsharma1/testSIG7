<%@ page import="com.rxlogix.util.ViewHelper; grails.util.Holders" %>

<div class="side-bar right-bar  caselist-config-panel no-one-enabled" id="quantitativeOnDemandFields" data-fieldcontainerreference="quantitativeFields" style="right: -413px; z-index: 9999">
    <div class="pv-list-config-action">
        <a class="ic-sm" href="javascript:void(0);" id="btnSaveListConfig" title="Save Changes"><i class="md md-done"></i> </a>
        <a class="text-danger ic-sm" href="javascript:void(0);" id="btnCloseListConfig" title="" data-original-title=""><i class="md md-close"></i> </a>
    </div>
    <div style="overflow: hidden" class="pv-grid-setting-box">
        <div class="col-sm-12 ">
            <div class="pvi-list-group list-group">
                <div class="inner-shadow-box">
                    <div class="phead-1 phead">Selected Fields</div>
                    <div class="list-group-primary display-config-field short-field-primary ui-sortable" ondragover="event.preventDefault()"><a href="#" class="list-group-item" data-field="listed">Listed</a>
                        <a href="#" class="list-group-item" data-field="newCount">New Count/Cum Count</a>
                        <a href="#" class="list-group-item" data-field="newSeriousCount">New Ser/Cum Ser</a>
                        <a href="#" class="list-group-item" data-field="newFatalCount">New Fatal/Cum Fatal</a>
                        <a href="#" class="list-group-item" data-field="newPediatricCount">New Paed/Cum Paed</a>
                        <a href="#" class="list-group-item" data-field="newStudyCount">New Study/Cum Study</a>
                        <a href="#" class="list-group-item" data-field="prrLCI">PRR LCI/PRR UCI</a>
                        <a href="#" class="list-group-item" data-field="prrValue">PRR</a>
                        <a href="#" class="list-group-item" data-field="rorLCI">ROR LCI/ROR UCI</a>
                        <a href="#" class="list-group-item" data-field="rorValue">ROR</a>
                        <a href="#" class="list-group-item" data-field="ebgm">EBGM</a>
                        <a href="#" class="list-group-item" data-field="eb05">EB05/EB95</a>
                    </div>
                </div>
                <div class="inner-shadow-box">
                    <div class="phead-2 phead">Optional Fields</div>
%{--                    <div class="list-group-optional display-config-field2 short-field ui-sortable" ondragover="event.preventDefault()"><a href="#" class="list-group-item" data-field="positiveDechallenge">+ve Dechallenge</a>--}%
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="rxmain-container rxmain-container-top" style="padding-right:5px;padding-left:5px">
    <div class="rxmain-container-inner">
        <!-- Header Section -->
        <div class="rxmain-container-row rxmain-container-header" style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <label class="rxmain-container-header-label" style="font-weight: bold; min-width: 200px">
                <g:message code="app.label.searchResults" />
            </label>
            <div class="ico-menu" style="display: flex; justify-content: flex-end; align-items: center; min-width: 50px; margin-right: 10px; z-index: 99">
                <span class="dropdown grid-icon" id="reportIconMenu">
                    <span class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false" style="cursor: pointer;">
                        <i class="mdi mdi-format-list-bulleted font-24" style="font-size: 21px;"></i>
                    </span>
                    <ul class="dropdown-menu ul-ddm" style="margin-top:0px">
                        <li class="li-pin-width">
                            <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#" id="configureQuantitativeFields" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert">
                                <i class="mdi mdi-settings-outline"></i>
                                <span tabindex="0">Field Selection</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-configureQuantitativeFields" title="Pin to top" data-toggle="collapse" data-title="Field selection"></span>
                            </a>
                        </li>
                        <li class="li-pin-width dropdown-submenu">
                            <a class="test text-left-prop" href="#">
                                <i class="mdi mdi-export"></i>
                                <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="x">Export To</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin active-pin" title="Unpin" data-title="Export To" data-id="#exportTypes"></span>
                            </a>
                            <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                                <strong class="font-12 title-spacing">Export</strong>
                                <li><a href="${Holders.config.dataMining.generateReport.word.url}">
                                    <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16">Save as Word
                                </a></li>
                                <li><a href="${Holders.config.dataMining.generateReport.excel.url}">
                                    <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16">Save as Excel
                                </a></li>
                                <li><a href="${Holders.config.dataMining.generateReport.pdf.url}">
                                    <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16">Save as PDF
                                </a></li>
                            </ul>
                        </li>
                        <li class="li-pin-width dropdown-submenu">
                            <a class="test text-left-prop" href="#">
                                <i class="mdi mdi-star-outline"></i>
                                <span tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="s">Save View</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin active-pin" title="Unpin" data-id="#saveViewTypes1" data-title="Save View"></span>
                            </a>
                            <ul class="dropdown-menu save-list col-min-150 ul-ddm-child" id="saveViewTypes">
                                <li><a href="#" tabindex="0" class="updateView ps5"><span>Save</span></a></li>
                                <li><a href="#" tabindex="0" class="saveView ps5"><span>Save As</span></a></li>
                                <li><a href="#" tabindex="0" class="editView ps5"><span>Edit</span></a></li>
                            </ul>
                        </li>
                    </ul>
                </span>
            </div>
        </div>


        <!-- Content Section -->
        <div class="rxmain-container-content rxmain-container-show" aria-expanded="true">
            <div class="row">
                <div class="row">
                    <div class="views-list col-md-12 bookmarkstrip bookmark-10 bookmark-pos" style="margin-top: 1px"><hr></div>

                </div>
                <div class="col-md-12">
                    <!-- DataTable -->
                    <div class="table-container">
                        <table id="alertsDetailsTable" class="display row-border no-shadow hover" width="100%">
                            <thead>
                            <tr id="alertsDetailsTableRow" class="evdas-header-row">
                                <th style=""><input id="select-all" type="checkbox"/></th>
                                <th class="th-label" style="min-width: 60px;"></th>
                                <th class="th-label" style="min-width: 200px;">Product Name</th>
                                <th class="th-label" style="min-width: 100px;">SOC</th>
                                <th class="th-label" style="min-width: 200px;">PT</th>
                                <th class="th-label" style="min-width: 100px;">HLT</th>
                                <th class="th-label" style="min-width: 100px;">Listed</th>
                                <th class="th-label" style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">New Count</div>
                                    <div class="stacked-cell-center-top">Cum Count</div>
                                </th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">New Ser</div>
                                    <div class="stacked-cell-center-top">Cum Ser</div>
                                </th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">New Fatal</div>
                                    <div class="stacked-cell-center-top">Cum Fatal</div>
                                </th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">New Paed</div>
                                    <div class="stacked-cell-center-top">Cum Paed</div>
                                </th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">New Study</div>
                                    <div class="stacked-cell-center-top">Cum Study</div>
                                </th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">PRR LCI</div>
                                    <div class="stacked-cell-center-top">PRR UCI</div>
                                </th>
                                <th class="stacked-cell-center-top" style="min-width: 100px; text-align: center !important;">PRR</th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">ROR LCI</div>
                                    <div class="stacked-cell-center-top">ROR UCI</div>
                                </th>
                                <th style="min-width: 100px; text-align: center !important;" class="stacked-cell-center-top th-label">ROR</th>
                                <th style="min-width: 100px; text-align: center !important;" class="stacked-cell-center-top th-label">EBGM</th>
                                <th style="min-width: 100px;" class="th-label stacked-column">
                                    <div class="stacked-cell-center-top">EB05</div>
                                    <div class="stacked-cell-center-top">EB95</div>
                                </th>
                            </tr>
                            </thead>
                        </table>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>
<style>

</style>
<asset:javascript src="app/pvs/dataMiningResult/dataMiningResult.js"/>

<!-- DataTables CSS and JS -->

<!-- Custom Styling -->
<style>
/* Set the header row background to white */
table {
}


.ico-menu {
    /* ... other styles ... */
    position: relative; /* Position the dropdown menu relative to its parent */
}

.ico-menu {
    /* ... other styles ... */
    position: relative; /* Position the dropdown menu relative to its parent */
}

.dropdown-menu {
    /* ... other styles ... */
    top: 100%; /* Position the dropdown menu directly below the icon */
    margin-top: -10px; /* Create a 5-pixel gap between the icon and the dropdown */
}

/* Set minimum width for each column in the table */



/* Scrollable container */


.table th, .table td {
    /*border: 1px solid #ddd;*/
    /*padding: 8px;*/
    text-align: center; /* Center align all cells */
}

.table tr:nth-child(even) {
    background-color: #f2f2f2;
}

.table .stacked-column {
    display: flex;
    flex-direction: column;
    align-items: center; /* Center align items within each stacked column */
}

.table .stacked-column div {
    text-align: center;
}

#alertsDetailsTable th,
#alertsDetailsTable td {
     /* Adjust as needed */
    white-space: nowrap; /* Prevent text from wrapping */
}
</style>

<!-- DataTable Initialization Script -->
<script>
    $(function() {
        // Ensure dropdowns work
        $('.dropdown-toggle').dropdown();

        $('#configureQuantitativeFields').on('click', function() {
            // Change the right property
            $('#quantitativeOnDemandFields').css('right', '0px'); // Ensure it's at its original position first
            // $('.dropdown-menu').hide()
        });

        $('#btnCloseListConfig').on('click', function() {
            // Change the right property
            $('#quantitativeOnDemandFields').css('right', '-413px'); // Ensure it's at its original position first

        });

    });

    $(function () {
        $('#alertsDetailsTable').DataTable({
            "paging": false,
            "searching": false,
            "lengthChange": false,
            "ordering": false,
            "info": false,
            "autoWidth": false,
            "scrollX": true, // Enable horizontal scrolling
            "scrollY": "1500px", // Set a fixed height for vertical scrolling (optional)
            "scrollCollapse": true, // Allow the table to reduce in size when fewer records
            "processing": true, // Enable processing mode
            "language": {
                "processing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>'
            },
        });
    });
</script>
