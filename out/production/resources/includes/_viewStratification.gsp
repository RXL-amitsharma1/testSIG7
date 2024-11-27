<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig; com.rxlogix.Constants; com.rxlogix.dto.SpotfireSettingsDTO; com.rxlogix.enums.ProductClassification; grails.converters.JSON; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.ExecutedConfiguration; com.sun.xml.internal.bind.v2.TODO; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeTypeCaseEnum;  com.rxlogix.config.DateRangeValue; com.rxlogix.enums.DrugTypeEnum ; grails.util.Holders" %>
<%@ page import="com.rxlogix.enums.EvaluateCaseDateEnum" %>
<g:if test="${isExecuted && configurationInstance?.selectedDatasource.contains(Constants.DataSource.PVA)&&(isEBGM||isPRR)}">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12"><label>Safety DB</label></div>
        </div>

        <div class="row">
            <div class="col-xs-6"><label>Stratification Safety DB:</label></div>

            <div class="col-xs-6">EBGM = <g:formatBoolean boolean="${isEBGM}" true="Yes" false="No"/></div>
        </div>
        <g:if test="${isEBGM}">
            <div class="row">
                <div class="col-xs-6"><label>EBGM Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label> ${stratificationMapEBGM.pva.age.join(', ')}</div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label> ${stratificationMapEBGM.pva.gender.join(", ")}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label> ${stratificationMapEBGM.pva.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-6"></div>

            <div class="col-xs-6">PRR = <g:formatBoolean boolean="${isPRR}" true="Yes" false="No"/></div>
        </div>
        <g:if test="${isPRR}">
            <div class="row">
                <div class="col-xs-6"><label>PRR/ROR/Chi-Square Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label> ${stratificationMapPRR.pva.age.join(', ')}</div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label> ${stratificationMapPRR.pva.gender.join(", ")}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label> ${stratificationMapPRR.pva.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>


        <g:if test="${stratificationMapEBGM.pva.isSubGroup}">
            <div class="row">
                <div class="col-xs-6"><label>Sub-Group Parameters</label></div>

                <div class="col-xs-6 pull-right"><label>Age Group:</label> ${stratificationMapEBGM.pva.ageSubGroup?.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label> ${stratificationMapEBGM.pva.genderSubGroup?.join(', ')}
                </div>
            </div>
            <div class="row">
                <div class="col-xs-6 pull-right"><label>Country:</label> ${stratificationMapEBGM.pva.countrySubGroup?.join(', ')}
                </div>
            </div>
            <div class="row">
                <div class="col-xs-6 pull-right"><label>Region:</label> ${stratificationMapEBGM.pva.regionSubGroup?.join(', ')}
                </div>
            </div>
        </g:if>
    </div>
</g:if>
<g:if test="${isExecuted && configurationInstance?.selectedDatasource.contains(Constants.DataSource.FAERS)&&(isFaersEBGM||isFaersPRR)}">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12"><label>FAERS</label></div>
        </div>

        <div class="row">
            <div class="col-xs-6"><label>Stratification Faers DB:</label></div>

            <div class="col-xs-6">EBGM = <g:formatBoolean boolean="${isFaersEBGM}" true="Yes"
                                                          false="No"/></div>
        </div>
        <g:if test="${isFaersEBGM}">
            <div class="row">
                <div class="col-xs-6"><label>EBGM Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapEBGM.faers.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapEBGM.faers.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapEBGM.faers.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-6"></div>

            <div class="col-xs-6">PRR = <g:formatBoolean boolean="${isFaersPRR}" true="Yes"
                                                         false="No"/></div>
        </div>
        <g:if test="${isFaersPRR}">
            <div class="row">
                <div class="col-xs-6"><label>PRR/ROR/Chi-Square Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapPRR.faers.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapPRR.faers.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapPRR.faers.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>


        <g:if test="${stratificationMapEBGM.faers.isSubGroup}">
            <div class="row">
                <div class="col-xs-6"><label>Sub-Group Parameters</label></div>

                <div class="col-xs-6 pull-right"><label>Age Group:</label>${" " + stratificationMapEBGM.faers.ageSubGroup.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapEBGM.faers.genderSubGroup.join(', ')}
                </div>
            </div>
        </g:if>
    </div>
</g:if>
<g:if test="${isExecuted && configurationInstance?.selectedDatasource.contains(Constants.DataSource.VAERS)&&(isVaersEBGM||isVaersPRR)}">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12"><label>VAERS</label></div>
        </div>

        <div class="row">
            <div class="col-xs-6"><label>Stratification Vaers DB:</label></div>

            <div class="col-xs-6">EBGM = <g:formatBoolean boolean="${isVaersEBGM}" true="Yes"
                                                          false="No"/></div>
        </div>
        <g:if test="${isVaersEBGM}">
            <div class="row">
                <div class="col-xs-6"><label>EBGM Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapEBGM.vaers.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapEBGM.vaers.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapEBGM.vaers.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-6"></div>

            <div class="col-xs-6">PRR = <g:formatBoolean boolean="${isVaersPRR}" true="Yes"
                                                         false="No"/></div>
        </div>
        <g:if test="${isVaersPRR}">
            <div class="row">
                <div class="col-xs-6"><label>PRR/ROR/Chi-Square Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapPRR.vaers.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapPRR.vaers.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapPRR.vaers.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>
    </div>
</g:if>
<g:if test="${isExecuted && configurationInstance?.selectedDatasource.contains(Constants.DataSource.VIGIBASE) && (isVigibaseEBGM||isVigibasePRR)}">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12"><label>VigiBase</label></div>
        </div>

        <div class="row">
            <div class="col-xs-6"><label>Stratification VigiBase DB:</label></div>

            <div class="col-xs-6">EBGM = <g:formatBoolean boolean="${isVigibaseEBGM}" true="Yes"
                                                          false="No"/></div>
        </div>
        <g:if test="${isVigibaseEBGM}">
            <div class="row">
                <div class="col-xs-6"><label>EBGM Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapEBGM.vigibase.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapEBGM.vigibase.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapEBGM.vigibase.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-6"></div>

            <div class="col-xs-6">PRR = <g:formatBoolean boolean="${isVigibasePRR}" true="Yes"
                                                         false="No"/></div>
        </div>
        <g:if test="${isVigibasePRR}">
            <div class="col-xs-6"><label>PRR/ROR/Chi-Square Stratification Parameters:</label></div>

            <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapPRR.vigibase.age.join(', ')}
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapPRR.vigibase.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapPRR.vigibase.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>
    </div>
</g:if>
<g:if test="${isExecuted && configurationInstance?.selectedDatasource.contains(Constants.DataSource.JADER) && (isJaderEBGM || isJaderPRR)}">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12"><label>JADER</label></div>
        </div>

        <div class="row">
            <div class="col-xs-6"><label>Stratification JADER DB:</label></div>

            <div class="col-xs-6">EBGM = <g:formatBoolean boolean="${isJaderEBGM}" true="Yes"
                                                          false="No"/></div>
        </div>
        <g:if test="${isJaderEBGM}">
            <div class="row">
                <div class="col-xs-6"><label>EBGM Stratification Parameters:</label></div>

                <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapEBGM.jader.age.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapEBGM.jader.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapEBGM.jader.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-6"></div>

            <div class="col-xs-6">PRR = <g:formatBoolean boolean="${isJaderPRR}" true="Yes"
                                                         false="No"/></div>
        </div>
        <g:if test="${isJaderPRR}">
            <div class="col-xs-6"><label>PRR/ROR/Chi-Square Stratification Parameters:</label></div>

            <div class="col-xs-6"><label>Age:</label>${" " + stratificationMapPRR.jader.age.join(', ')}
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Gender:</label>${" " + stratificationMapPRR.jader.gender.join(', ')}
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6 pull-right"><label>Receipt Years:</label>${" " + stratificationMapPRR.jader.receiptYear.join(', ')}
                </div>
            </div>
        </g:if>
    </div>
</g:if>
