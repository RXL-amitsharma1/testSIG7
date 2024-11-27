<%@ page import="com.rxlogix.signal.SubstanceFrequency; com.rxlogix.util.DateUtil;java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat;com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter;"%>
%{--<button class="btn btn-primary pull-right"  data-target="#loginModal_freq" data-toggle="modal" >&#43</button>--}%

<head>
<title>Substance Frequency</title>
</head>
<body>
            <g:form method="post" url="[controller:'SubstanceFrequency',action:'newFrequency']">
                <div class="form-group">
                    <label for= "frequency">Frequency Label</label>
                    <input class="form-control" placeholder = "Frequency" name="frequencyName" type = "text" id="frequency">
                </div>
                <div class="form-group">
                    <label for= "product-name">Product Name</label>
                    <input class="form-control" placeholder = "Product Name" name="name" type = "text" id="product-name">
                </div>

                <div class="form-group">
                    <div class="col-md-6">
                        <div class="fuelux">
                            <div class="datepicker" id="start-date-picker_freq">
                                <label>Start Date<span class="required-indicator">*</span></label>
                                <div class="input-group">
                                    <input placeholder="Start Date" name="startDate" class="form-control input-sm startDate" id="startDate_freq"
                                           type="text" data-date="" value=""/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="fuelux">
                            <div class="datepicker" id="end-date-picker_freq">
                                <label>End Date<span class="required-indicator">*</span></label>
                                <div class="input-group">
                                    <input placeholder="End Date" name="endDate" class="form-control input-sm endDate" id="endDate_freq" type="text"
                                           data-date="" value=""/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
                <br> </br>


                <div class="form-group">
                    <label for= "u-frequency">Upload Frequency</label>
                    <input class="form-control" placeholder = "Upload Frequency" name="uploadFrequency" type = "text" id="u-frequency">
                </div>
                <div class="form-group">
                    <label for= "m-frequency">Mining Frequency</label>
                    <input class="form-control" placeholder = "Mining Frequency" name="miningFrequency" type = "text" id="m-frequency">
                </div>
                <div class="form-group">
                    <label for= "alert-type">Alert Type</label>
                    <select class="form-control" name="alertType" id="alert-type">
                        <option value="select">--select--</option>
                        <option value="quantitativeAlert">Quantitative Alert</option>
                        <option value="EvdasAlert">Evdas Alert</option>
                    </select>
                </div>
                </div>
                <div class="modal-footer">
                <body class="buttons ">
                <g:submitButton name="save" value="Save" class="btn btn-primary"></g:submitButton>
                <button class="btn btn-primary" data-dismiss="modal">Close</button>
            </g:form>

        </div>
</body>