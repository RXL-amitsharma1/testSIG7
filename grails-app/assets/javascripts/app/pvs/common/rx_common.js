var signal = signal || {};

// a convenience function for parsing string namespaces and
// automatically generating nested namespaces
function extend( ns, ns_string ) {
    var parts = ns_string.split('.'),
        parent = ns,
        pl, i;
    if (parts[0] == "signal") {
        parts = parts.slice(1);
    }
    pl = parts.length;
    for (i = 0; i < pl; i++) {
        //create a property if it doesnt exist
        if (typeof parent[parts[i]] == 'undefined') {
            parent[parts[i]] = {};
        }
        parent = parent[parts[i]];
    }
    return parent;
}

//Prototype methods
Date.prototype.addDays = function(days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() + days);
    return date;
};

Array.prototype.remove = function (value) {
    return this.filter(function(f){f != value});
};

SCA_WORKFLOW_STATUS_ENUM = { NEW: 'New',
    ASSOCIATE_REVIEWED: 'AssociateReviewed',
    PHYSICIAN_REVIEWED: 'PhysicianReviewed',
    REQUIRED_EVALUATION: 'RequiredEvaluation',
    CONTINUED_MONITORING: 'ContinuedMonitoring'
};

SCA_DISPOSITION_ENUM = {
    VALIDATED_SIGNAL : 'ValidatedSignal',
    VALIDATED_NON_CONFIRMED_SIGNAL : 'ValidatedNonConfirmedSignal',
    VALIDATED_UNDER_INVESTIGATION : 'ValidatedUnderInvestigation',
    COMMUNICATED_SIGNAL : 'CommunicatedSignal',
    NON_VALID: 'NonValid'
};

SCA_PRIORITY_ENUM = {
    HIGH: "High",
    MEDIUM: "Medium",
    LOW: "LOW"
};

DATE_FMT_TZ = "YYYY-MM-DD";

signal.utils = (function() {

    var stacked = function(topValue, bottomValue) {
        var topComp = "";
        var bottomComp = "";

        if(_.isFunction(topValue)) {
            topComp = topValue()
        } else {
            topComp = '<div class="stacked-cell-center-top">' + topValue + '</div>'
        }

        if (_.isFunction(bottomValue)) {
            bottomComp = bottomValue()
        } else {
            bottomComp = '<div class="stacked-cell-center-bottom">' + bottomValue + '</div>'
        }

        return topComp + bottomComp
    };

    var stackedForClass = function(topValue, bottomValue, classNameTop, classNameBottom) {
        var topComp = "";
        var bottomComp = "";

        if(_.isFunction(topValue)) {
            topComp = topValue()
        } else {
            topComp = '<div class="stacked-cell-center-top '+ classNameTop +'">' + topValue + '</div>'
        }

        if (_.isFunction(bottomValue)) {
            bottomComp = bottomValue()
        } else {
            bottomComp = '<div class="stacked-cell-center-bottom '+ classNameBottom +'">' + bottomValue + '</div>'
        }

        return topComp + bottomComp
    };

    // And this is the definition of the custom function ​
    var render = function(tmpl_name, tmpl_data) {

        if ( !render.tmpl_cache ) {



            render.tmpl_cache = {};
        }
        if (!render.tmpl_cache[tmpl_name]) {
            var tmpl_dir = '/signal/assets/app/pvs/hbs-templates';
            var tmpl_url = tmpl_dir + '/' + tmpl_name + '.hbs';

            var tmpl_string = "";
            $.ajax({
                url: tmpl_url,
                method: 'GET',
                async: false,
            }).done(function(data) {
                tmpl_string = data
            });

            render.tmpl_cache[tmpl_name] = Handlebars.compile(tmpl_string);
        }

        return render.tmpl_cache[tmpl_name](tmpl_data)
    };

    var hbs_partial = function(tmpl_name) {
        if (!hbs_partial.tmpl_cache) {
            hbs_partial.tmpl_cache = {}
        }

        if (!hbs_partial.tmpl_cache[tmpl_name]) {
             var tmpl_dir = '/signal/assets/app/pvs/hbs-templates';
            var tmpl_url = tmpl_dir + '/' + tmpl_name + '.hbs';

            var tmpl_string = "";
            $.ajax({
                url: tmpl_url,
                method: 'GET',
                async: false,
            }).done(function(data) {
                tmpl_string = data
            });

            hbs_partial.tmpl_cache[tmpl_name] = tmpl_string
        }

        return hbs_partial.tmpl_cache[tmpl_name]
    };

    var composeUrl = function(controller, action, params) {
        var url = "/signal/" + controller + "/" + action + (_.isNull(params) ? '' : '?' + composeParams(params));

        return url
    };

    //TODO : Need to change this to handlebar form, Will be done later
    var postUrl = function (path, params, newWindow) {
        var dynamicWindow = 'w_' + Date.now() + Math.floor(Math.random() * 100000).toString();
        const form = document.createElement('form');
        form.method = "post";
        form.action = path;
        form.enctype = "application/x-www-form-urlencoded";
        if (newWindow) form.target = dynamicWindow;

        var token = $("meta[name='_csrf']").attr("content");
        var parameter = $("meta[name='_csrf_parameter']").attr("content");
        params[parameter] = token;
        for (let key in params) {
            if (params.hasOwnProperty(key)) {
                const hiddenField = document.createElement('input');
                hiddenField.type = 'hidden';
                hiddenField.name = key;
                hiddenField.value = params[key];
                form.appendChild(hiddenField);
            }
        }
        document.body.appendChild(form);
        if (newWindow) window.open('',dynamicWindow);
        form.submit();
    }

    var composeParams = function(o) {
        return _.map(_.pairs(o), function(p){return p.join('=')} ).join('&')
    };

    var capitalIt = function(s) {
        return s && s[0].toUpperCase() + s.slice(1);
    };

    var breakIt = function(s) {
        return s ? s.split(/(?=[A-Z])/).join(' ') : s
    };

    var enable_load_button = function(ele, enabled) {
        return function(evt) {
            var targetEle = ele.find('.glyphicon');
            if (enabled) {
                $(targetEle).addClass('refresh-animate');
            } else {
                $(targetEle).removeClass('refresh-animate');
            }
        }
    };
    var setInLocalStorage = function(prop, data) {
        localStorage.setItem(prop, data);
    };

    var getFromLocalStorage = function(prop) {
        return localStorage.getItem(prop);
    };

    var setJSONInLocalStorage = function(prop, data) {
        localStorage.setItem(prop, JSON.stringify(data));
    };

    var getJSONFromLocalStorage = function (prop) {
        return JSON.parse(localStorage.getItem(prop));
    };

    var localStorageUtil = {
        setProp : setInLocalStorage,
        getProp : getFromLocalStorage,
        setJSON : setJSONInLocalStorage,
        getJSON : getJSONFromLocalStorage
    };

    var getQueryString = function() {
        var key = false, res = {}, itm = null;
        // get the query string without the ?
        var qs = location.search.substring(1);
        // check for the key as an argument
        if (arguments.length > 0 && arguments[0].length > 1)
            key = arguments[0];
        // make a regex pattern to grab key/value
        var pattern = /([^&=]+)=([^&]*)/g;
        // loop the items in the query string, either
        // find a match to the argument, or build an object
        // with key/value pairs
        while (itm = pattern.exec(qs)) {
            if (key !== false && decodeURIComponent(itm[1]) === key)
                return decodeURIComponent(itm[2]);
            else if (key === false)
                res[decodeURIComponent(itm[1])] = decodeURIComponent(itm[2]);
        }

        return key === false ? res : null;
    };

    return {
        render : render,
        stacked: stacked,
        stackedForClass: stackedForClass,
        composeUrl: composeUrl,
        postUrl: postUrl,
        composeParams: composeParams,
        capitalIt: capitalIt,
        breakIt: breakIt,
        hbs_partial: hbs_partial,
        enable_load_button: enable_load_button,
        localStorageUtil: localStorageUtil,
        getQueryString: getQueryString
    }
})();

var fetchAcceptedFileFormats = function() {
    $.ajax({
        url: '/signal/attachmentable/fetchAcceptedFileFormats' ,
    }).done(function (result) {
        $(".attachment-file").attr("accept" , result.join(","))
    });
};

