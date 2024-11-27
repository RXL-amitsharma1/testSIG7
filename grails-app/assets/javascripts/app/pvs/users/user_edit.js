$(function () {

    var init_page = function () {
        $("#locale").select2();
        $("#timeZone").select2();
        $("#username").select2({
            placeholder: "Enter a username",
            minimumInputLength: 3,
            multiple: false,
            ajax: {
                quietMillis: 100,
                dataType: "json",
                url: "/signal/user/ajaxLdapSearch",
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data,
                        pagination: {
                            more: (params.page * 10) < data.length
                        }
                    };
                }
            }
        }).on('select2:select', function (e) {
            $('#username').val(e.params.data.id);
        });
    }

    var getUrl = function () {

    }

    init_page();

    $('.pv-switch input').each((ind, item) => {
        $(item).prop('checked', $(item).attr('data-value') === 'true')
    })
});


