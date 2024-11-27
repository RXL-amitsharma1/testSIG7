$(function() {

    $("#exportTechConfig").on('click', function () {

        $.ajax({
            url: "/signal/configManagement/generateConfigurationFile"
        })
            .done(function (result) {
                if (result.status) {
                    $.Notification.notify('success', 'top right', "Success", "User will be notified once file is generated.", {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", "An Error occurred while exporting file.", {autoHideDelay: 5000});
                }
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while exporting file. Please re-upload file and try again.", {autoHideDelay: 5000});
        })
    });



    $("#compareConfigurations").on('click', function () {

        var form = $('#compareConfigForm')[0];
        var data = new FormData()
        data.append("configFileFirst",$("#config_first_file")[0].files[0]);
        data.append("configFileSecond", $("#config_second_file")[0].files[0]);
        $.ajax({
            url: "/signal/configManagement/compareConfigurations",
            data: data,
            type: "POST",
            contentType: false,
            processData: false
        })
            .done(function (result) {
                if (result.status) {
                    $.Notification.notify('success', 'top right', "Success", "User will be notified once file is generated.", {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", "An Error occurred while exporting file.", {autoHideDelay: 5000});
                }
                $("#config_first_file").val(null);
                $("#config_second_file").val(null);
                $("#config1_file_name").val('');
                $("#config2_file_name").val('');
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while exporting file. Please re-upload file and try again.", {autoHideDelay: 5000});
                $("#config_first_file").val(null);
                $("#config_second_file").val(null);
                $("#config1_file_name").val('');
                $("#config2_file_name").val('');
        })
    });


    $("#ImportConfiguration").on('click', function () {

        var form = $('#importConfigForm')[0];
        var data = new FormData()
        if ($("#busConfig").is(":checked")) {
            data.append("configType", $("#busConfig").val());
        } else {
            data.append("configType", $("#techConfig").val());
        }
        data.append("appName",$("#appName").val());
        data.append("configFile",$("#config_file_input")[0].files[0]);
        $.ajax({
            url: "/signal/configManagement/importDataFromFile",
            data: data,
            type: "POST",
            contentType: false,
            processData: false
        })
            .done(function (result) {
                if (result.status) {
                    $.Notification.notify('success', 'top right', "Success", "User will be notified once file is generated.", {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", "An Error occurred while importing file.", {autoHideDelay: 5000});
                }
                $("#config_file_input").val(null);
                $("#config_file_name").val('');
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while exporting file. Please re-upload file and try again.", {autoHideDelay: 5000});
                $("#config_file_input").val(null);
                $("#config_file_name").val('');
        })
    });


    $("#refreshTechnicalConf").on('click', function () {
        $(this).addClass('glyphicon-refresh-animate');
        $('#refreshTechnicalConf').off('click');
        $('#refreshTechnicalConf').css("cursor", "not-allowed");
        $.ajax({
            url: "/signal/configManagement/refreshTechConfig",})
            .done(function (result) {
                if (result.status) {
                    $.Notification.notify('success', 'top right', "Success", "Technical Configuration refresh triggered.", {autoHideDelay: 5000});
                    $('#refreshTechnicalConf').removeClass('glyphicon-refresh-animate');
                    $('#lastRefreshTime').val(result.data);

                } else {
                    $.Notification.notify('warning', 'top right', "Warning", "Unable to fetch status. Data refresh maybe taking more time than expected. Please check again later.", {autoHideDelay: 5000});
                    $('#refreshTechnicalConf').removeClass('glyphicon-refresh-animate');
                }
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while refreshing technical configurations.", {autoHideDelay: 5000});
                $('#refreshTechnicalConf').removeClass('glyphicon-refresh-animate');
            })
    });
});