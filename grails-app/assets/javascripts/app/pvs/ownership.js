$(function() {

    $("#collapse1").on('show.bs.collapse', function (e) {
         loadData(listReportsUrl + "/" + e.delegateTarget.dataset.id, "#collapse1");
    });

    $(".owner-select").on("select2-selecting", function (e) {

        var fcurrent = (function() {
            var cid = e.val;
            return function() {return cid;}
        })();

        var fcurrentName = (function() {
            var cname = e.choice.text;
            return function() {return cname;}
        })();

        current = fcurrent();
        currentName = fcurrentName();

        if(current != previous) {
            $('#changeOwnerModal').modal('show');
        }

    });

    $('#changeOwnerModal').on('show.bs.modal', function () {
        var modal = $(this);

        modal.find('.previousFullName').text(previousName);
        modal.find('.currentFullName').html(currentName);

        //create new action
        var ctx = window.location.pathname;

        var newAction = "/" + ctx.split("/")[1] + "/ownershipRest/changeOwners/" + previous + "?current=" + current;

        $("#ownerChangeButton").closest("form").attr("action", newAction);
    });

    $('#changeOwnerModal').on('hide.bs.modal', function () {
          $("#owner").select2('data', {text:previousName});

    })

    var current;
    var currentName;

    $(".owner-select").select2({width:"element",
        allowClear: true
        });

    var loadData = function(url, elem){
        $.ajax({
            url: url,
            dataType: "json"
        })
            .done(function(data) {
                var items = [];
                $.each( data, function( id, value ) {
                    items.push( "<li class='list-group-item'  id='" + id + "'><a>" + value[1] + "</a></li>" );
                });

                $( "<ul/>", {
                    "class": "list-group",
                    html: items.join( "" )
                }).appendTo(elem);
        });
    }



});

