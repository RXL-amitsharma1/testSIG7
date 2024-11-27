/*
 * classList.js: Cross-browser full element.classList implementation.
 * 2012-11-15
 *
 * By Eli Grey, http://eligrey.com
 * Public Domain.
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 */

/*global self, document, DOMException */

/*! @source http://purl.eligrey.com/github/classList.js/blob/master/classList.js*/

// This column-view-lib.js is the one shown in Configuration. Edit this one.
if (typeof document !== "undefined" && !("classList" in document.documentElement)) {

    (function (view) {

        "use strict";

        if (!('HTMLElement' in view) && !('Element' in view)) return;

        var
            classListProp = "classList"
            , protoProp = "prototype"
            , elemCtrProto = (view.HTMLElement || view.Element)[protoProp]
            , objCtr = Object
            , strTrim = String[protoProp].trim || function () {
                    return this.replace(/^\s+|\s+$/g, "");
                }
            , arrIndexOf = Array[protoProp].indexOf || function (item) {
                    var
                        i = 0
                        , len = this.length
                        ;
                    for (; i < len; i++) {
                        if (i in this && this[i] === item) {
                            return i;
                        }
                    }
                    return -1;
                }
        // Vendors: please allow content code to instantiate DOMExceptions
            , DOMEx = function (type, message) {
                this.name = type;
                this.code = DOMException[type];
                this.message = message;
            }
            , checkTokenAndGetIndex = function (classList, token) {
                if (token === "") {
                    throw new DOMEx(
                        "SYNTAX_ERR"
                        , "An invalid or illegal string was specified"
                    );
                }
                if (/\s/.test(token)) {
                    throw new DOMEx(
                        "INVALID_CHARACTER_ERR"
                        , "String contains an invalid character"
                    );
                }
                return arrIndexOf.call(classList, token);
            }
            , ClassList = function (elem) {
                var
                    trimmedClasses = strTrim.call(elem.className)
                    , classes = trimmedClasses ? trimmedClasses.split(/\s+/) : []
                    , i = 0
                    , len = classes.length
                    ;
                for (; i < len; i++) {
                    this.push(classes[i]);
                }
                this._updateClassName = function () {
                    elem.className = this.toString();
                };
            }
            , classListProto = ClassList[protoProp] = []
            , classListGetter = function () {
                return new ClassList(this);
            }
            ;
        // Most DOMException implementations don't allow calling DOMException's toString()
        // on non-DOMExceptions. Error's toString() is sufficient here.
        DOMEx[protoProp] = Error[protoProp];
        classListProto.item = function (i) {
            return this[i] || null;
        };
        classListProto.contains = function (token) {
            token += "";
            return checkTokenAndGetIndex(this, token) !== -1;
        };
        classListProto.add = function () {
            var
                tokens = arguments
                , i = 0
                , l = tokens.length
                , token
                , updated = false
                ;
            do {
                token = tokens[i] + "";
                if (checkTokenAndGetIndex(this, token) === -1) {
                    this.push(token);
                    updated = true;
                }
            }
            while (++i < l);

            if (updated) {
                this._updateClassName();
            }
        };
        classListProto.remove = function () {
            var
                tokens = arguments
                , i = 0
                , l = tokens.length
                , token
                , updated = false
                ;
            do {
                token = tokens[i] + "";
                var index = checkTokenAndGetIndex(this, token);
                if (index !== -1) {
                    this.splice(index, 1);
                    updated = true;
                }
            }
            while (++i < l);

            if (updated) {
                this._updateClassName();
            }
        };
        classListProto.toggle = function (token, forse) {
            token += "";

            var
                result = this.contains(token)
                , method = result ?
                forse !== true && "remove"
                    :
                forse !== false && "add"
                ;

            if (method) {
                this[method](token);
            }

            return !result;
        };
        classListProto.toString = function () {
            return this.join(" ");
        };

        if (objCtr.defineProperty) {
            var classListPropDesc = {
                get: classListGetter, enumerable: true, configurable: true
            };
            try {
                objCtr.defineProperty(elemCtrProto, classListProp, classListPropDesc);
            } catch (ex) { // IE 8 doesn't support enumerable:true
                if (ex.number === -0x7FF5EC54) {
                    classListPropDesc.enumerable = false;
                    objCtr.defineProperty(elemCtrProto, classListProp, classListPropDesc);
                }
            }
        } else if (objCtr[protoProp].__defineGetter__) {
            elemCtrProto.__defineGetter__(classListProp, classListGetter);
        }

    }(self));

}
function debounce(func, wait, immediate) {
    var timeout, args, context, timestamp, result;

    function now() {
        new Date().getTime();
    }

    var later = function () {
        var last = now() - timestamp;
        if (last < wait) {
            timeout = setTimeout(later, wait - last);
        } else {
            timeout = null;
            if (!immediate) {
                result = func.apply(context, args);
                context = args = null;
            }
        }
    };

    return function () {
        context = this;
        args = arguments;
        timestamp = now();
        var callNow = immediate && !timeout;
        if (!timeout) {
            timeout = setTimeout(later, wait);
        }
        if (callNow) {
            result = func.apply(context, args);
            context = args = null;
        }

        return result;
    };
}


var ColumnView = (function () {
    "use strict";

    var keyCodes, _slice, transformPrefix;

    keyCodes = {
        enter: 13,
        space: 32,
        backspace: 8,
        tab: 9,
        left: 37,
        up: 38,
        right: 39,
        down: 40,
    };

    _slice = Array.prototype.slice;

    transformPrefix = getTransformPrefix();

    function getTransformPrefix() {
        var el = document.createElement("_");
        var prefixes = ["transform", "webkitTransform", "MozTransform", "msTransform", "OTransform"];
        var prefix;
        while (prefix = prefixes.shift()) {
            if (prefix in el.style) return prefix;
        }
        console.warn("transform not supported");
        return null;
    }

    function uid() {
        return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
    }

    function ColumnView(el, options) {
        if (!ColumnView.canBrowserHandleThis()) {
            throw "This browser doesn't support all neccesary EcmaScript 5 Javascript methods.";
        }

        var that = this, onKeydown, onKeyup, resize;

        this.options = options || {};
        this.value = null;
        this.ready = false;

        this.el = el;
        this.domCarriage = el.$.carriage;
        this.carriage = document.createDocumentFragment();
        this.style = el.$.style;

        this.models = options.items;
        this.path = options.path;
        this.dictionaryType = options.dictionaryType;

        this.scroll = options.scroll;
        this.movingUpOrDown = false;
        this.colCount = 3; //default

        this.callbacks = {
            change: that.options.onChange,
            source: that.options.source,
            ready: that.options.ready
        };

        this.setLayout(options.layout);

        if (options.itemTemplate) {
            this.CustomSelect.prototype.itemTemplate = options.itemTemplate;
        }

        // this.uniqueClassName = "column-view-" + uid();
        // this.el.classList.add(this.uniqueClassName);
        this.el.setAttribute("tabindex", 0);
        this.el.setAttribute("role", "tree");

        // bound functions
        onKeydown = this._onKeydown.bind(this);
        onKeyup = this._onKeyup.bind(this);
        resize = debounce(this._resize.bind(this), 300);
        this._onColumnChangeBound = this._onColumnChange.bind(this);
        // onResize = _.bind(this._onResize, this);

        this.el.addEventListener("keydown", onKeydown, true);
        this.el.addEventListener("keyup", onKeyup, true);
        window.addEventListener("resize", resize);

        // todo prevent scroll when focused and arrow key is pressed
        // this.el.addEventListener("keydown", function(e){e.preventDefault();});

        this._initialize();
    }

    ColumnView.canBrowserHandleThis = function canBrowserHandleThis() {
        return !!Array.prototype.map && !!Array.prototype.forEach && !!Array.prototype.map && !!Function.prototype.bind;
    };

    // instance methods
    // ----------------

    ColumnView.prototype = {

        // Getter
        // --------

        columns: function columns() {
            return _slice.call(this.carriage.children);
        },

        focusedColumn: function focusedColumn() {
            var cols = this.columns();
            return cols[cols.length - 2] || cols[0];
        },

        canMoveBack: function canMoveBack() {
            if (this.colCount === 3)
                return this.columns().length > 2;
            else
                return this.columns().length > 1;
        },


        // Keyboard
        // --------

        _onKeydown: function onKeydown(e) {
            this.movingUpOrDown = false;

            if (e.altKey || e.ctrlKey || e.shiftKey || e.metaKey)
                return; // do nothing

            if (e.keyCode == keyCodes.enter) {
                // simulate add button in dictionary modal
                if (this.dictionaryType === EVENT_DICTIONARY) {
                    addDictionary(eventValues, EVENT_DICTIONARY);
                } else if (this.dictionaryType === PRODUCT_DICTIONARY) {
                    addDictionary(productValues, PRODUCT_DICTIONARY);
                } else if (this.dictionaryType === STUDY_DICTIONARY) {
                    addDictionary(studyValues, STUDY_DICTIONARY);
                }
            }

//            switch (e.keyCode) {
//                case keyCodes.left:
//                case keyCodes.backspace:
//                    this._keyLeft();
//                    e.preventDefault();
//                    break;
//                case keyCodes.right:
//                case keyCodes.space:
//                case keyCodes.enter:
//                    this._keyRight();
//                    e.preventDefault();
//                    break;
//                case keyCodes.up:
//                    this.movingUpOrDown = true;
//                    this._moveCursor(-1);
//                    e.preventDefault();
//                    break;
//                case keyCodes.down:
//                    this.movingUpOrDown = true;
//                    this._moveCursor(1);
//                    e.preventDefault();
//                    break;
//                default:
//                    return;
//            }
        },

        _onKeyup: function onKeyup() {
            this.movingUpOrDown = false;
            if (this.fastMoveChangeFn) this.fastMoveChangeFn();
        },

        _keyLeft: function keyLeft() {
            this.back();
        },

        _keyRight: function keyRight() {
            var col = this.carriage.lastChild;
            if (col.customSelect) col.customSelect.selectIndex(0); // COL ACTION!!!!!!
            // triggers change
        },

        _moveCursor: function moveCursor(direction) {
            var col = this.focusedColumn();
            col.customSelect.movePosition(direction);
        },

        _resetColumns: function resetColumns(exceptIndex) {
            for (var i = 0; i < this.columns().length; i++) {
                if (i != exceptIndex) {
                    this.columns()[i].innerHTML = "";
                }
            }
        },

        _clearAll: function clearAll(dictionaryLevel) {
            var that = this;
            var clearButton = ".clearEventValues";
            if (dictionaryLevel == PRODUCT_DICTIONARY) {
                clearButton = ".clearProductValues";
            } else if (dictionaryLevel == STUDY_DICTIONARY) {
                clearButton = ".clearStudyValues";
            }
            $('#mainContent').on("columnView.reset." + dictionaryLevel, function (event) {
                resetDictionaryList(dictionaryLevel);
                clearAllText(dictionaryLevel);
                clearSearchInputs(-1, dictionaryLevel);
                that._resetColumns(-1);
                $('.errorMessage').hide();
            });

            $('#mainContent').on('click', clearButton, function () {
                $('#mainContent').trigger("columnView.reset." + dictionaryLevel);
            });
        },

        _onColumnChange: function onColumnChange(columnClass, value, oldValue, isCtrlPressed) {
            var that = this;
            var column = columnClass.el;
            if (!this.ready) return;

            if (this.movingUpOrDown) {
                this.fastMoveChangeFn = function () {
                    that._onColumnChange(columnClass, value, oldValue, isCtrlPressed);
                };
                return;
            }

            this.fastMoveChangeFn = null;
            // console.log("cv change", value)

            this.value = value;

//            this.lastColEl = this.columns()[this.columns().indexOf(column) + 1];
            this.nextColEl = this.columns()[this.columns().indexOf(column) + 1];
            this.preColEl = this.columns()[this.columns().indexOf(column) - 1];
            this.currentColEl = this.columns()[this.columns().indexOf(column)];

            if (this.preColEl && this.preColEl.querySelector(".highlighted") && !this.currentColEl.querySelector(".highlighted") && !isCtrlPressed) {
                // if change to another search result
                that._resetColumns(this.columns().indexOf(column)); // reset other columns
            }

            if (!isCtrlPressed) {
                var preSelectedItems = that.carriage.querySelectorAll(".selectedBackground");
                if (preSelectedItems.length > 0) {
                    [].forEach.call(preSelectedItems, function (item) {
                        item.classList.remove("selectedBackground");
                        item.classList.remove("selected");
                    });
                }
            }

            var selectedItems = that.currentColEl.querySelectorAll(".selected");

            // Select multiple items by holding ctrl button
            // note: querySelectorAll isn't an array, it's a NodeList, and not all browsers support forEach on NodeList's.
            // Current implementation is a quick way to iterate over all the found elements: works for all browsers
            [].forEach.call(selectedItems, function(item) {
                item.classList.add("selectedBackground");
            });

            this.callbacks.source(value, appendIfValueIsSame, columnClass.dictionaryLevel, that.dictionaryType, true);

            function appendIfValueIsSame(data, level) {
                if (that.value !== value) return;
                that.currentColEl = that.columns()[level - 1];
                that.nextColEl = that.columns()[level];

                // PVR-3016: Event dictionary checks this level to not attempt to highlight past the synonym column (6).
                if (level < 6) {
                    if (that.nextColEl && that.nextColEl.querySelector(".highlighted")) { // in search path
                        var newData = generateNewDataForCol(data, that.nextColEl);
                        that._appendCol(newData, level, 1, isCtrlPressed);
                        that.callbacks.source(newData.selectedPath, appendIfValueIsSame, parseInt(level) + 1, that.dictionaryType, false);
                    //    Needed to add check if its last level of current Dictionary
                    } else if (!that.currentColEl.querySelector(".highlighted") && level < that.columns().length) {
                        that._appendCol(data, level, 1, isCtrlPressed);
                    }
                }
                that.callbacks.change.call(that, value);
                sourceParents(value, columnClass.dictionaryLevel, showParents, that.dictionaryType);
            }

            function showParents(data, level) {
                //console.log("|||||show parents data: ", data, level);
                if (data && level > 1) {
                    that.preColEl = that.columns()[level - 2];
                    that.currentColEl = that.columns()[level - 1];

                    if (that.currentColEl.querySelector(".highlighted") && that.preColEl && that.preColEl.querySelector(".highlighted")) {
                        var newData;
                        if (isCtrlPressed) {
                            newData = data;
                        } else {
                            newData = generateNewDataForCol(data, that.preColEl);
                        }
                        that._appendCol(newData, level, -1, isCtrlPressed);
                    } else if (that.preColEl && (that.preColEl.querySelector(".highlighted") || !that.preColEl.querySelector("li"))) {
                        var highlighted = false;

                        // Highlight primary SOC if PT clicked
                        if (level == 2) {
                            if (selectedDictionaryValue.primarySOC) {
                                var socIndex = -1;
                                _.each(data.items, function (soc, i) {
                                    if (soc.id == selectedDictionaryValue.primarySOC) {
                                        socIndex = i;
                                    }
                                });
                                data.highlightedValue = data.items[socIndex].id; // select one from search result
                                highlighted = true;
                            }
                        }

                        // Highlight primary HLGT if PT clicked
                        if (level == 3) {
                            if (selectedDictionaryValue.primaryHLGT) {
                                var hlgtIndex = -1;
                                _.each(data.items, function (hlgt, i) {
                                    if (hlgt.id == selectedDictionaryValue.primaryHLGT) {
                                        hlgtIndex = i;
                                    }
                                });
                                data.highlightedValue = data.items[hlgtIndex].id; // select one from search result
                                highlighted = true;
                            }
                        }

                        // Highlight primary HLGT if PT clicked
                        if (level == 4) {
                            if (selectedDictionaryValue.primaryHLT) {
                                //alert('selectedDictionaryValue.primaryHLT: ' + selectedDictionaryValue.primaryHLT + 'data items: ' + data.items);
                                var hltIndex = -1;
                                _.each(data.items, function (hlt, i) {
                                    if (hlt.id == selectedDictionaryValue.primaryHLT) {
                                        hltIndex = i;
                                    }
                                });
                                data.highlightedValue = data.items[hltIndex].id; // select one from search result
                                highlighted = true;
                            }
                        }

                        if (!highlighted) {
                            // Not clicking from PT
                            data.highlightedValue = data.items[0].id; // select one from search result
                        }

                        that._appendCol(data, level, -1, isCtrlPressed);
                    }
                    that.callbacks.change.call(that, value);

                    if (that.preColEl && (that.preColEl.querySelector(".highlighted") || !that.preColEl.querySelector("li"))) {
                        var idList = [];
                        for (var i = 0; i < data.items.length; i++) {
                            idList.push(data.items[i].id)
                        }
                        sourceParents(idList, level - 1, showParents, that.dictionaryType);
                    }
                }
            }

//            console.log("*-*-*-*-*-on column change: ", columnClass, this, value, oldValue);

            // todo handle case case no callback is called
        },

        // Calls the source callback for each value in
        // this.path and append the new columns
        _initialize: function initialize() {
            var that = this;
            var path = this.path || [];
            var pathPairs = path.map(function (value, index, array) {
                return [value, array[index + 1]];
            });
//            console.log("path -> ", path, ", pathPairs -> ", pathPairs);

            this.carriage.innerHTML = "";

//            function proccessPathPair(pathPair, cb) {
//                var id = pathPair[0], nextID = pathPair[1];
//                var customSelect;
//                that.callbacks.source(String(id), function (data) {
//                    if (nextID) data.selectedValue = String(nextID);
//                    customSelect = that._appendCol(data, "SOC");
//                    cb();
//                }); // add level here
//            }
//
//            function proccessPath() {
//                var pathPair = pathPairs.shift();
//                if (pathPair)
//                    proccessPathPair(pathPair, proccessPath);
//                else
//                    ready();
//            }

            function ready() {
                that.domCarriage.innerHTML = "";
                that.domCarriage.appendChild(that.carriage);
                that.carriage = that.domCarriage;

                var emptyData = {items: []};
                switch (that.dictionaryType) {
                    case EVENT_DICTIONARY:
                        that._appendCol(emptyData, "SOC", that.isCtrlPressed);
                        that._appendCol(emptyData, "HLGT", that.isCtrlPressed);
                        that._appendCol(emptyData, "HLT", that.isCtrlPressed);
                        that._appendCol(emptyData, "PT", that.isCtrlPressed);
                        that._appendCol(emptyData, "LLT", that.isCtrlPressed);
                        that._appendCol(emptyData, "Synonyms", that.isCtrlPressed);
                        break;
                    case PRODUCT_DICTIONARY:
                        that._appendCol(emptyData, "Ingredient", that.isCtrlPressed);
                        that._appendCol(emptyData, "Family", that.isCtrlPressed);
                        that._appendCol(emptyData, "Product Generic Name", that.isCtrlPressed);
                        that._appendCol(emptyData, "Trade Name", that.isCtrlPressed);
                        break;
                    case STUDY_DICTIONARY:
                        that._appendCol(emptyData, "StudyNumber", that.isCtrlPressed);
                        that._appendCol(emptyData, "ProtocolNumber", that.isCtrlPressed);
                        that._appendCol(emptyData, "Center", that.isCtrlPressed);
                        break;
                }

                that._inputSearch();

                that._clearAll(that.dictionaryType);

                that._resize();
                that._alignCols();
                that.ready = true;
                if (that.callbacks.ready) that.callbacks.ready();
            }

            ready();
        },

        _inputSearch: function inputSearch() {
            var that = this;
            var searchEvents = getSearchInputs(this.el).getElementsByClassName('searchEvents');
            addListenerToInput(searchEvents);
            var searchProducts = getSearchInputs(this.el).getElementsByClassName('searchProducts');
            addListenerToInput(searchProducts);
            var searchStudies = getSearchInputs(this.el).getElementsByClassName('searchStudies');
            addListenerToInput(searchStudies);

            function getSearchInputs(el) {
                return el.parentElement.parentElement.parentElement;
            }

            function addListenerToInput(inputList) {
                for (var i = 0; i < inputList.length; ++i) {
                    $(inputList[i]).on(
                        {
                            keydown: function (e) {
                                if (e.which == 13 || e.keyCode == 13) { // 13 is enter
                                    searchDictionary(this, inputList);
                                }
                            },
                            focusout: function () {
                                searchDictionary(this, inputList);
                            }
                        });
                }
            }

            function searchDictionary(selectedInput, inputList) {
                var searchTerm = selectedInput.value;
                if (searchTerm.trim() != '') {
                    var searchInput = Array.prototype.slice.call(inputList);
                    clearSearchInputs(searchInput.indexOf(selectedInput), that.dictionaryType);
                    that._resetColumns(-1); // reset all columns before a new search
                    var level = selectedInput.getAttribute('level');
                    that.currentColEl = that.columns()[level - 1];
                    forSearch(searchTerm, level, showSearchResult, that.dictionaryType);
                }
            }

            function showSearchResult(data, level) {
                that._appendCol(data, level, 0, that.isCtrlPressed);
//                that.callbacks.change.call(that, value);
            }
        },

        _appendCol: function appendCol(data, dictionaryLevel, colDiff, isCtrlPressed) {
            var col = this._createCol(data, dictionaryLevel, colDiff, isCtrlPressed);
            if (this.ready) this._alignCols();
//            this.lastColEl = null;
            return col;
        },

        _createCol: function createCol(data, dictionaryLevel, colDiff, isCtrlPressed) {
            var col;
            // use existing col if possible
//            console.log("create col: ", colDiff, dictionaryLevel, this.nextColEl, this)
            if (this.nextColEl || this.currentColEl) {
                if (colDiff == 0) col = this.currentColEl;
                else if (colDiff == -1) col = this.preColEl;
                else col = this.nextColEl;

                if (!isCtrlPressed) {
                    col.innerHTML = "";
                }
                // col.selectIndex = null;
            } else {
                col = document.createElement("ul");
                col.classList.add("dicUlFormat");
                switch (this.dictionaryType) {
                    case EVENT_DICTIONARY:
                        col.classList.add("eventDictionaryColWidth");
                        break;
                    case PRODUCT_DICTIONARY:
                        col.classList.add("productDictionaryColWidth");
                        break;
                    case STUDY_DICTIONARY:
                        col.classList.add("studyDictionaryColWidth");
                        break;
                }
                col.setAttribute("dictionaryLevel", dictionaryLevel);
                // col.classList.add("column");
                this.carriage.appendChild(col);
            }
            return this._newColInstance(data, col, isCtrlPressed);
        },

        _newColInstance: function newColInstance(data, col, isCtrlPressed) {
            var colInst;
            if (col.customSelect) col.customSelect.clear();
            if (data.dom) {
                //        colInst = new this.Preview(col, data.dom);
                // reset monkeypatched properties for reused col elements
            }
            else if (data.items || data.groups) {
                data.onChange = this._onColumnChangeBound;
                colInst = new this.CustomSelect(col, data, isCtrlPressed);
            }
            else {
                throw "Type error";
            }
            return colInst;
        },

        _removeAfter: function removeAfter(col) {
            var cols = this.columns();
            var toRemove = cols.splice(cols.indexOf(col) + 1, cols.length);
            var that = this;
            toRemove.forEach(function (ul) {
                ul.innerHTML = ''
            });
            //      toRemove.forEach(function(col) { that.carriage.removeChild(col); });
        },

        _alignCols: function alignCols() {
            var length = this.columns().length;
            if (this.lastAllignment === length)
                return; // skip if nothing has changed

            this.lastAllignment = length;
            var leftOut = Math.max(0, length - this.colCount);
            this.lastLeftOut = leftOut
            //      this._moveCarriage(leftOut);
        },

        _resize: function resize() {
            this.colWidth = this.carriage.childNodes[0].getBoundingClientRect().width;
            //      this._moveCarriage(this.lastLeftOut, {transition: false});
        },

        setLayout: function setLayout(layout) {
            // console.log("setLayout", layout);
            if (layout == "mobile") {
                this.colCount = 1;
                this.el.classList.add("mobile");
            } else {
                this.colCount = 3;
                this.el.classList.remove("mobile");
            }
            if (!this.ready) return;
            this._resize();
        },

        _moveCarriage: function moveCarriage(leftOut, options) {
            if (this.scroll) {
                this.domCarriage.scrollLeft = this.domCarriage.getBoundingClientRect().width;
                return;
            }
            options = options || {};
            if (!options.hasOwnProperty("transition")) options.transition = true
            this.lastLeftOut = leftOut;
            // console.log("move", this.ready)
            var left = -1 * leftOut * this.colWidth;
            this.domCarriage.classList.toggle("transition", this.ready && options.transition);
            this.domCarriage.style[transformPrefix] = "translate(" + left + "px, 0px)";
        },

        // ### public

        back: function back() {
            if (!this.canMoveBack()) return;
            var lastCol = this.focusedColumn();
            //      this._removeAfter(lastCol);
            // triggers no change
            //if (lastCol.customSelect)
            lastCol.customSelect.deselect(); // COL ACTION!!!!!!

            this._alignCols();
            this.value = this.focusedColumn().customSelect.value();
            this.callbacks.change.call(this, this.value);
        }


    };

    return ColumnView;


})();


function htmlToDocumentFragment(html) {
    "use strict";
    var frag = document.createDocumentFragment();
    var tmp = document.createElement("body");
    tmp.innerHTML = html;
    var child;
    while (child = tmp.firstChild) {
        frag.appendChild(child);
    }
    return frag;
}


ColumnView.prototype.CustomSelect = (function () {
    "use strict";

    var indexOf = Array.prototype.indexOf;

    // aria-owns="catGroup" aria-expanded="false"
    // https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_group_role

    function CustomSelect(parent, data, isCtrlPressed) {
        if (!data) data = {};

        this.el = parent;

        this.models = data.items;
        this.groups = data.groups;
        this.changeCB = data.onChange;
        this.isCtrlPressed = isCtrlPressed;

        this._selectedEl = this.el.querySelector(".selected");
        this._selectedPath = this.el.querySelector(".selectedPath");
        this._highlightedEl = this.el.querySelector(".highlighted");
        this.items = this.el.querySelectorAll("li");

        this.value = null;
        this.dictionaryLevel = null;

        // this.el.setAttribute("role", "group");

        this.boundOnClick = this._onClick.bind(this);
        this.el.addEventListener("click", this.boundOnClick);

        this._monkeyPatchEl();

        if (this.models || this.groups) this._render(data.selectedValue, data.highlightedValue, data.selectedPath);
    }

    // instance methods
    // ----------------

    CustomSelect.prototype = {

        _monkeyPatchEl: function monkeyPatchEl() {
            var that = this;
            var selectIndex = this.selectIndex.bind(this);
            var movePosition = this.movePosition.bind(this);
            var deselect = this.deselect.bind(this);
            var clear = this.clear.bind(this);
            var selectValue = this.selectValue.bind(this);
            var selectPath = this.selectPath.bind(this);
            var highlightValue = this.highlightValue.bind(this);
            var elMethods = {
                selectIndex: selectIndex,
                movePosition: movePosition,
                deselect: deselect,
                selectValue: selectValue,
                selectPath: selectPath,
                highlightValue: highlightValue,
                clear: clear,
                value: function value() {
                    return that.value;
                }
            };
            this.el.customSelect = elMethods;
        },

        _render: function render(selectedValue, highlightedValue, selectedPath) {
            var container = document.createDocumentFragment();

            if (this.groups) {
                this._renderGroups(container, this.groups);
            }
            else if (this.models) {
                this._renderItems(container, this.models, this.el);
            }
            else {
                this._renderEmpty(container);
            }

            if (this.isCtrlPressed) {
                selectedPath = highlightedValue;
            }

            this.items = this.el.querySelectorAll("li");
            if (selectedValue) this.selectValue(selectedValue);
            if (selectedPath) this.selectPath(selectedPath);
            if (highlightedValue) this.highlightValue(highlightedValue);
        },

        _renderItems: function renderItems(container, models, oldItem) {
            var that = this;
            var oldItems = oldItem.querySelectorAll("li");
            var existingItems = [];
            [].forEach.call(oldItems, function (item) {
                existingItems.push(item.getAttribute("data-value"));
            });
            var innerContent = oldItem.innerHTML;
            models.forEach(function (model) {
                if (!existingItems.includes(model.id.toString())) {
                    innerContent += that.itemTemplate(model);
                }
            });
            oldItem.innerHTML = innerContent;
        },

        _renderGroups: function renderGroups(container, groups) {
            var that = this;
            groups.forEach(function (group) {
                var html = that.groupTemplate(group);
                var item = htmlToDocumentFragment(html);
                container.appendChild(item);
                that._renderItems(container, group.items);
            });
        },

        _renderEmpty: function renderEmpty(container) {
            var el = document.createTextNode("empty");
            container.appendChild(el);
        },

        clear: function clear() {
            this.el.customSelect = null;
            this.el.removeEventListener("click", this.boundOnClick);
        },

        _scrollIntoView: function scrollIntoView() {
            var elRect = this.el.getBoundingClientRect();
            var itemRect = this._selectedEl.getBoundingClientRect();

            if (itemRect.bottom > elRect.bottom) {
                this.el.scrollTop += itemRect.bottom - elRect.bottom;
            }

            if (itemRect.top < elRect.top) {
                this.el.scrollTop -= elRect.top - itemRect.top;
            }
        },

        _deselect: function deselect(el) {
            el.classList.remove("selected");
            this._selectedEl = null;
        },

        _select: function select(el, _ctrlPressed) {
            var oldValue;
            var allSelectedElems = this.el.querySelectorAll(".selectedBackground");
            if (_ctrlPressed) {
                var flag = false;
                oldValue = this.value;
                [].forEach.call(allSelectedElems, function (item) {
                    if (item === el) {
                        flag = true;
                    }
                });

                if (flag) {
                    this._deselect(el);
                    el.classList.remove("selectedBackground");
                } else {
                    el.classList.add("selected");
                    this._selectedEl = el;
                    this.value = el.getAttribute("data-value");
                    this.dictionaryLevel = el.getAttribute("dictionaryLevel");
                    this.changeCB(this, this.value, oldValue, true);
                }
            } else {
                if (this._selectedEl) this._deselect(this._selectedEl);
                var highlighted = this.el.querySelector(".selected");
                if (highlighted) {
                    highlighted.classList.remove("selected");
                } else {
                    this._selectedEl = el;
                }
                el.classList.add("selected");
                oldValue = this.value;
                this.value = el.getAttribute("data-value");
                this.dictionaryLevel = el.getAttribute("dictionaryLevel");
                this.changeCB(this, this.value, oldValue, false);
            }
        },

        _onClick: function onClick(e) {
            var _ctrlPressed = (e.metaKey || e.ctrlKey);

            if (e.target.tagName == "LI") {
                e.preventDefault();
                this._select(e.target, _ctrlPressed);
            }
        },

        _getActiveIndex: function getActiveIndex() {
            var active = this._selectedEl;
            var index = indexOf.call(this.items, active);
            return index;
        },

        movePosition: function movePosition(direction) {
            var index = this._getActiveIndex();
            this.selectIndex(index + direction);
        },

        selectIndex: function selectIndex(index) {
            var item = this.items[index];
            if (item) this._select(item);
            this._scrollIntoView();
        },

        // ### public

        remove: function remove() {
            this.el.remove();
        },

        deselect: function deselect() {
            if (this._selectedEl) this._deselect(this._selectedEl);
        },

        selectValue: function selectValue(value) {
            var el = this.el.querySelector("[data-value='" + value + "']");
            this._select(el);
        },

        selectPath: function selectPath(value) {
            var highlighted = this.el.querySelector(".selected");
            if (highlighted) highlighted.classList.remove("selected");
            var el = this.el.querySelector("[data-value='" + value + "']");
            el.classList.add("selected");
        },

        highlightValue: function highlightValue(value) {
            var el = this.el.querySelector("[data-value='" + value + "']");
            el.classList.add("highlighted");
            var selected = this.el.querySelector(".selected");
            if (!selected) el.classList.add("selected");
        },

        itemTemplate: function itemTemplate(data) {
            return '<li class= "dicLi" dictionaryLevel="' + data.level + '" data-value="' + data.id + '">' + data.name + '</li>';
        },

        groupTemplate: function groupTemplate(data) {
            return '<div class="divider">' + data.title + '</div>';
        }

    };

    return CustomSelect;

})();


ColumnView.prototype.Preview = (function () {
    "use strict";

    function Preview(parent, el) {
        this.el = parent;
        this.el.appendChild(el);
        this.el.classList.add("html");
    }

    Preview.prototype = {
        remove: function remove() {
            this.el.remove();
        }
    };
    return Preview;
})();

