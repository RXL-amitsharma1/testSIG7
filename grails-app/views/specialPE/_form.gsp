<g:render template="/includes/layout/flashErrorsDivs" bean="${specialPE}" var="theInstance"/>
<div class="row">
    <div class="col-md-12">
        <div>
            <div class="col-md-6" style="border-bottom:solid lightgrey">
                <label>Special Products</label>
            </div>
        </div>
        <div>
            <div class="col-md-6" style="border-bottom:solid lightgrey">
                <label>Special Events</label>
            </div>
        </div>
    </div>
</div>
<br/>
<div class="row">
    <div class="col-md-12">
        <div>
            <div class="col-md-6">
                <g:select id="specialProductList" name="specialProductList" from="${productsList}"
                          multiple="true" value="${prodListObj}">
                </g:select>
            </div>
        </div>
        <div>
            <div class="col-md-6">
                <g:select id="specialEventList" name="specialEventList" from="${eventsList}"
                          multiple="true" value="${eventListObj}">
                </g:select>
            </div>
        </div>
    </div>
</div>
<input type="hidden" value="${specialPE.id}" name="id"/>
