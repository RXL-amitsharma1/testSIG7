<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" %>
<style>
    .dropdown{ cursor: pointer !important;}
    .grid-pin{ cursor: pointer !important;}
</style>

    <div class="row">
        <div class="col-md-4"></div>
        <div class="col-md-8 grid_margin">
            <div class="pos-rel pull-right ">
                <span style="padding-top:20px" >
                    <span class="pull-left " style="padding-top:5px" >

                    <!------------------=================--------------pinned icon code started-------------================----------------------->
                    <a class="grid-pin collapse theme-color" id="ic-importConfiguration" title="Import Configuration" data-toggle="modal" data-target="#importConfigurationFileModal">
                        <i class="mdi mdi-import font-24"></i>
                    </a>
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color exportPanel" id="ic-exportToExcel" title="Export To Excel">
                        <i class="mdi mdi-export font-24"></i>
                    </a>
                    <a  class="grid-pin collapse theme-color import-log-modal" id="ic-importLog" title="Import Log">
                        <i class="mdi mdi-import font-24"></i>
                    </a>
                    <!------------------=================--------------pinned icon code closed--------------================----------------------->
                    </span>

                    <!------------------------------------------------------------import menu code start----------------------------------------------------------------------------->
                    <span class="dropdown grid-icon" id="importConfigurationIconMenu">
                        <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                            <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i></span>
                        <ul class="dropdown-menu ul-ddm"  style="width: 210px">

                            <li class="li-pin-width">
                                <a class="test field-config-bar-toggle text-left-prop " id="importConfigurationMenu" data-toggle="modal" data-target="#importConfigurationFileModal" >
                                    <i class="mdi mdi-import"></i>
                                    <span tabindex="0">
                                        Import Configuration
                                    </span>
                                </a>

                                <a href="javascript:void(0)" class="text-right-prop" >
                                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-importConfiguration" title="Pin to top"  data-toggle="collapse"  data-title="Import Configuration"></span>
                                </a>

                            </li>
                            <!----------===================--------------export to excel menu code start------------===================----------------------->
                            <li class="li-pin-width dropdown-submenu">
                                <a class="test text-left-prop" href="#">
                                    <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                                    Export To Excel
                                </span>
                                </a>
                                <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-title="Export To Excel" data-id="#ic-exportToExcel"></span>
                                </a>
                            </li>

                        <!---------------=================---------export to excel menu code end----------==============--------------------->
                        <!---------------=================---------import  Log menu code start----------==============--------------------->
                            <li class="li-pin-width dropdown-submenu">
                                <a class="test text-left-prop" href="#">
                                    <i class="mdi mdi-import"></i> <span tabindex="0" class="dropdown-toggle grid-menu-tooltip import-log-modal"  accesskey="x" >
                                    Import Log
                                </span>
                                </a>
                                <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-title="Import Log" data-id="#ic-importLog"></span>
                                </a>
                            </li>
                        <!---------------=================---------export Log menu code end----------==============--------------------->



                        </ul>
                    </span>
                    <!-------------------------------------list menu ended---------------------------------------------------------------------------------------------->



                </span>

            </div>
        </div>
        <!------------------------====-----code end---------------===========-------------->

    </div>

