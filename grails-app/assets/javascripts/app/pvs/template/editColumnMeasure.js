$(function () {

    // For create page
    if ($('.columnMeasureSet').length == 1) {
        addColumnMeasureDiv(0);
        columnSetList.push([]);
        $('#columnMeasureContainer').width($('#columnMeasureContainer').width() - 460); // for scroll
    } else {
        var contentWidth = getColMeasTotalWidth();
        $('#columnMeasureContainer').width(contentWidth + 150);
    }

    $('#addColumnMeasure').on('click', function() {
        addColumnMeasureDiv(numColMeas);
        initMeasureSelect2(numColMeas);
        columnSetList.push([]);
        numColMeas++;

        $('.removeColumnMeasure').show();
    });

    $(document).on('click', '.removeColumnMeasure', function () {
        var container = this.closest('.columnMeasureSet');
        var index = $(container).attr('sequence');
        measureIndexList[index] = -1;
        columnSetList[index] = [];

        var containerWidth = $(container.parentElement).width();
        containerWidth -= $(container).width();
        $(container.parentElement).width(containerWidth);

        container.remove();

        if ($('.columnMeasureSet').length == 2) { // including the hidden gsp template
            $('.columnMeasureSet').find('.removeColumnMeasure').hide();
        }
    });


});

function addColumnMeasureDiv(index) {
    var container = $('#columnMeasureContainer');

    // clone template for colMeas; change id and sequence
    var cloned = $('#colMeas_template').clone();
    cloned.removeAttr('id');
    cloned.attr('sequence', index);

    cloned.find('#selectMeasure').attr('id', 'selectMeasure'+index);

    cloned.find('#colMeas-validMeasureIndex').attr('id', 'colMeas'+index+'-validMeasureIndex')
        .attr('name', 'colMeas'+index+'-validMeasureIndex');

    cloned.find('#columns').attr('id', 'columns'+index)
        .attr('name', 'columns'+index);

    var containerWidth = $(container).width();
    container.append(cloned);
//    var contentWidth = getColMeasTotalWidth(container[0], 'columnMeasureSet');
//    if (contentWidth > containerWidth) {
        containerWidth += $(cloned).width();
        $(container).width(containerWidth);
//    }
}

function getColMeasTotalWidth() {
    var totalWidth = 0;
    $.each($('#columnMeasureContainer').find('.columnMeasureSet'), function () {
        totalWidth += $(this).width() + 10;
    });
    return totalWidth;
}
