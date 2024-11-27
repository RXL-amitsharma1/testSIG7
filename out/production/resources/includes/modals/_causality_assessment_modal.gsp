<style>
input[type="checkbox"],
input[type="radio"],
input.radio,
input.checkbox {
    vertical-align:text-top;
    width:13px;
    height:13px;
    padding:0;
    margin:0;
    position:relative;
    overflow:hidden;
    top:2px;
}

.demographyTable {
    border-collapse: collapse;
    width: 100%;
    border: 1px solid #ddd;
    text-align: left;
}

.demographyTableTd {
    padding: 15px;
    border: 1px solid #ddd;
    text-align: left;
}
</style>
<div class="modal fade" id="causalityModal" class="causalityModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Causality Assessment</label>
            </button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-sm-12">
                        <table class="table table-condensed modal-table-casuality">
                            <thead>
                            <tr>
                                <th class="col-md-5">Category</th>
                                <th class="col-md-2" id="causalityValue"></th>
                                <th colspan="2">Score</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr data-toggle="collapse" data-target="#row1-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Temporality</td>
                                <td></td>
                                <td>100</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row1-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">
                                        <tbody>
                                        <tr><td class="col-md-8">Temporal Relationship</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes" selected="">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">100</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr data-toggle="collapse" data-target="#row2-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Biogradient</td>
                                <td></td>
                                <td>75</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row2-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">
                                        <tbody>
                                        <tr><td class="col-md-8">Rechallenge</td>
                                            <td class="col-md-2">
                                                <select class="">
                                                    <option value="yes">Yes</option>
                                                    <option value="no">No</option>
                                                    <option value="unknown" selected="">Unknown</option>
                                                </select>
                                            </td>
                                            <td class="col-md-2">50</td></tr>
                                        <tr><td class="col-md-8">Dechallenge</td>
                                            <td class="col-md-2">
                                                <select class="">
                                                    <option value="yes" selected="">Yes</option>
                                                    <option value="no">No</option>
                                                    <option value="unknown">Unknown</option>
                                                </select>
                                            </td>
                                            <td class="col-md-2">100</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr data-toggle="collapse" data-target="#row3-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Evidence Strength</td>
                                <td></td>
                                <td>50</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row3-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">
                                        <tbody>
                                        <tr><td class="col-md-8">PRR</td>
                                            <td class="col-md-2">
                                                <select class="">
                                                    <option value="yes">Yes</option>
                                                    <option value="no">No</option>
                                                    <option value="unknown" selected="">Unknown</option>
                                                </select>
                                            </td>
                                            <td class="col-md-2">50</td></tr>
                                        <tr><td class="col-md-8">IC</td>
                                            <td class="col-md-2">
                                                <select class="">
                                                    <option value="yes">Yes</option>
                                                    <option value="no">No</option>
                                                    <option value="unknown" selected="">Unknown</option>
                                                </select>
                                            </td>
                                            <td class="col-md-2">50</td></tr>
                                        <tr><td class="col-md-8">Trend</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown" selected="">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">50</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr data-toggle="collapse" data-target="#row4-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Specificity</td>
                                <td></td>
                                <td>95</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row4-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">
                                        <tbody>
                                        <tr><td class="col-md-8">Listedness</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes">Yes</option>
                                                <option value="no" selected="">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">0</td></tr>
                                        <tr><td class="col-md-8">DME</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes" selected="">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">100</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr data-toggle="collapse" data-target="#row5-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Coherence</td>
                                <td></td>
                                <td>45</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row5-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">

                                        <tbody>
                                        <tr><td class="col-md-8">Concomitant Drug</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes" selected="">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">100</td></tr>
                                        <tr><td class="col-md-8">Confounding Indication</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes">Yes</option>
                                                <option value="no" selected="">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">0</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr data-toggle="collapse" data-target="#row6-detail" class="accordion-toggle collapsed" aria-expanded="false">
                                <td>Consistency</td>
                                <td></td>
                                <td>80</td>
                                <td><button class="btn btn-primary btn-xs pull-right"><i class="ion-minus-round"></i></button></td>
                            </tr>
                            <tr class="hiddenRow collapse" id="row6-detail">
                                <td colspan="4" class="p-0">
                                    <table class="table table-striped table-bordered">
                                        <tbody>
                                        <tr><td class="col-md-8">Multiple Country Evidence</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes" selected="">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown" selected="">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">50</td></tr>
                                        <tr><td class="col-md-8">Case Type Evidence</td>
                                            <td class="col-md-2"> <select class="">
                                                <option value="yes" selected="">Yes</option>
                                                <option value="no">No</option>
                                                <option value="unknown">Unknown</option>
                                            </select></td>
                                            <td class="col-md-2">100</td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr class="collapsed">
                                <td class="col-md-8" style="font-weight: bold;">BH Criteria Likelihood	</td>
                                <td colspan="2" style="text-align: right;position: absolute;right: 13.5%;font-weight: bold;">73%</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn btn-xs btn-default expandAll pull-left">Expand All</a>
                <a href="#" class="btn btn-xs btn-default collapseAll pull-left m-l-5">Collapse All</a>

                <button type="button" class="btn pv-btn-grey waves-effect" data-dismiss="modal">Close</button>
            </div>
            <div class="ui-resizable-handle ui-resizable-e" style="z-index: 90;"></div>
            <div class="ui-resizable-handle ui-resizable-s" style="z-index: 90;"></div>
            <div class="ui-resizable-handle ui-resizable-se ui-icon ui-icon-gripsmall-diagonal-se" style="z-index: 90;"></div>
        </div>
    </div>
</div>

