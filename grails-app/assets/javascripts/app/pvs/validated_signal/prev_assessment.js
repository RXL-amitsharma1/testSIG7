$(function() {

    var prevAssessment = null;

    $(".previous-assessment").on('click', function () {
        var modal = $('#previousAssessmentModal');
        modal.find('#prev-assessment-table');
        init_prev_assessment_table()
        modal.modal("show");
    });

    var init_prev_assessment_table = function() {

        if (typeof prevAssessment != "undefined" && prevAssessment != null) {
            prevAssessment.destroy()
        }
        prevAssessment = $('#prev-assessment-table').DataTable({
            processing: true,
            serverSide: true,
            bPaginate: false,
            info: false,
            filter: false,
            columns: [
                {
                    data: "name",
                    "sWidth" : "9"

                },
                {
                    data: "term",
                    "sWidth" : "9"
                },
                {
                    data: "disposition",
                    "sWidth" : "9"
                },
                {
                    data: "dateClosed",
                    "sWidth" : "7"
                },
                {
                    data: "lastReviewedDate",
                    "sWidth" : "7"
                },
                {
                    data: "comments",
                    render: function (data, type, row, meta) {
                        if (row.comments.length > 0) {
                            var contents = "<url>\n";
                            _.each(row.comments, function(item){
                                contents += "<li>" + item + "</li>"
                            });

                            return contents;
                        } else {
                            return ""
                        }
                    },
                    "sWidth" : "20"
                },
                {
                    data: 'actions',
                    render: function (data, type, row, meta) {
                        if (row.actions.length > 0) {
                            var actions = "<url>\n";
                            _.each(row.actions, function(item){
                                actions += "<li>" + item + "</li>"
                            });

                            return actions;
                        } else {
                            return ""
                        }
                    },
                    "sWidth" : "20"
                }
            ],
            ajax: {
                url: previousAssessmentUrl,
                dataSrc: '',
                type: 'POST'
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    }
})