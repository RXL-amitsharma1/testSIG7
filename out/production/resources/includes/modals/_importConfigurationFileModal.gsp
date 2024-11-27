<div class="modal fade" id="importConfigurationFileModal">
    <div class="modal-dialog modal-md">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Import Configuration</label>
            </div>

            <form action="upload" enctype="multipart/form-data" id="importConfigurationFileUploadForm" method="post">

                <div class="modal-body"><input type="hidden" id="importConfTypeId" name="importConfigurationType" value="Single Case Alert">

                    <div class="form-group">
                        <div class="file-uploader" data-provides="fileupload">
                            <input type="file" name="file" id="file"
                                   data-mandatory="false" data-mandatoryset="0" class="file" accept=".xlsx,.xls,.csv" >

                            <div class="input-group">
                                <input type="text" class="form-control " placeholder="Attach a file"
                                       id="configuration-file-name" name="configuration-file-name" value=""
                                       title="">

                                <span class="input-group-btn ">
                                    <button class="browse btn btn-primary btn-file-pa-upload " type="button"><i
                                            class="glyphicon glyphicon-search" id="upload-attachment"></i>
                                    </button>
                                </span>

                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancelUploadModalButton" data-dismiss="modal">
                        Cancel
                    </button>
                    <input type="submit" class="btn primaryButton btn-primary upload" value="Upload"
                           id="submit-button" disabled>
                </div>
            </form>
        </div>
    </div>
</div>