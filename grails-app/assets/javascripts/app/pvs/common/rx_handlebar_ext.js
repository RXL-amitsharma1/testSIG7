Handlebars.registerHelper('i18n',
    function(str){
        return ( (typeof i18n) !== 'undefined' ? str : str)
    }
);

Handlebars.registerHelper('select', function(name, selectedValue, options, disp_field, value_field) {
    var out = "<select class='form-control selectBox' id=\'" + name + "\' name=" + name + ">\n";
    _.each(options, function(v) {
        out += "<option value='" + v[value_field] + "'" +
            (v[value_field] == selectedValue[value_field] ? " selected " : "") + " >" +
            escapeHTML(v[disp_field]) + "</option>\n"
    });
    out += "</select>";

    return new Handlebars.SafeString(out)
});
//Added for action dropdown
Handlebars.registerHelper('selectForAction', function(name, selectedValue, options, disp_field, value_field) {
    var out = "<select class='form-control selectBox' id=\'" + name + "\' name=" + name + ">\n";
    _.each(options, function(v) {
        out += "<option value='" + v[value_field] + "'" +
            (v[value_field] == selectedValue[value_field] ? " selected " : "") + " >" +
            escapeAllHTML(v[disp_field]) + "</option>\n"
    });
    out += "</select>";

    return new Handlebars.SafeString(out)
});

Handlebars.registerPartial('date_picker_template', signal.utils.hbs_partial('date_picker_template'));

//Handlebar helper to imitate the if conditions
Handlebars.registerHelper('if_eq', function(a, b, opts) {
    if (a == b) {
        return opts.fn(this);
    }
});
Handlebars.registerHelper('ifVaersOnly', function(a, b, c, d, e, opts) {
    if ((a||d) && !b && !c && !e) {
        return opts.fn(this);
    }
    return opts.inverse(this);
});

//Handlebar helper to imitate the if..else conditions
Handlebars.registerHelper('if_else_eq', function(a, b, opts) {
    if (a == b) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('if_notNull', function(key, opts) {
    if(key!=null && key!=undefined && key!="null"){
        return opts.fn(this);
    } else{
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('if_else_neq', function(a, b, opts) {
    if (a != b) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

// To check if string a is present in list b or not
Handlebars.registerHelper('if_else_not_in', function(a, b, opts) {
    if (!b.includes(a)) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('if_isClosed', function(closed, isClosed, opts) {
    if(closed || isClosed){
        return opts.fn(this);
    } else{
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('if_odd_key', function (key, opts) {
    if (key % 2 == 0) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('compareCell', function (value, columnName, row, tableName, compareData, opts) {
    var out = fetchComparedContent(value, columnName, row, tableName, compareData);
    return new Handlebars.SafeString(out);
});

Handlebars.registerHelper("setVar", function (varName, varValue, options) {
    options.data.root[varName] = varValue;
});

Handlebars.registerHelper("if_else_referenceType", function (rowData, value, opts) {
    var index = -1;
    var count = 0;
    $.each(rowData, function (key, data) {
        if (value == data) {
            index = count;
        }
        count++;
    });

    if (index % 2 != 0 && rowData['Reference Type'] == 'Duplicate Cases') {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

Handlebars.registerHelper('left_iteration', function (key, versionCompareData, versionAvailableData, block) {
    var out = "";

    if (key && versionCompareData != undefined && versionAvailableData != undefined && versionCompareData[key] != undefined && versionCompareData[key] != null) {
        var comparisonRows = versionCompareData[key];
        var availableRows = versionAvailableData[key];
        var result = [];
        if(comparisonRows[0]['containsValues']) {
            $.each(comparisonRows, function (column, value) {
                var comparedVersionRow = findComparedVersionRow(availableRows, uniqueKeyMap[key], value)
                if (comparedVersionRow == undefined || comparedVersionRow == null) {
                    isChangePresent = true
                    result.push(value);
                }
            });
        }
        $.each(result, function (key, value) {
            out += block.fn(value);
        })
    }
    return out;
});

Handlebars.registerHelper('print_left_rows', function (key, block) {
    if (key != null && key != undefined && !jQuery.isEmptyObject(key)) {
        return new Handlebars.SafeString(fetchDiff(key, ""));
    } else {
        return "-";
    }

});

Handlebars.registerHelper('contains_value', function (data,key,compareData, opts) {
    if (data[0]['containsValues'] || (compareData[key] && compareData[key][0]['containsValues'])) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
    return opts.fn(this);
});

Handlebars.registerHelper('lowerCase', function (data, opts) {
    var out = data?.toLowerCase().replaceAll(" ", "_") + "_container";
    return new Handlebars.SafeString(out);
});

Handlebars.registerHelper('each_subTags_compared_screen', function (tagText, tagType, privateUser, tags, block) {
    var out = "";
    var result = [];
    $.each(tags, function (key, value) {
        if(value.tagText == tagText && value.tagType == tagType && value.privateUser == privateUser){
            var subTags = [];
            if(value.subTagText == undefined || value.subTagText == null || value.subTagText == "") {
                subTags = null
            } else{
                subTags = value.subTagText.split(";")
            }
            value['subTags'] = subTags
            result.push(value);
        }
    });

    $.each(result, function (key, value) {
        out += block.fn(value);
    })

    return out;
});
