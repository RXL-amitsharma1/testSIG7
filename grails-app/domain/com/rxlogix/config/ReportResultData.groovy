package com.rxlogix.config

import com.rxlogix.util.DbUtil

import java.util.zip.GZIPInputStream

class ReportResultData {
    static auditable = false
    String crossTabHeader
    byte[] value
    String reportSQL
    String querySQL
    String versionSQL
    String gttSQL
    String headerSQL

    static belongsTo = [reportResult: ReportResult]

    static constraints = {
        value(maxSize: 1024*1024*50, nullable:true)         // 50MB
        crossTabHeader(maxSize: 32*1024, nullable:true)     // 32k
        reportSQL(maxSize: 32*1024, nullable:true)          // 32k
        querySQL(maxSize: 32*1024, nullable:true)           // 32k
        versionSQL(maxSize: 32*1024, nullable:true)         // 32k
        gttSQL(maxSize: 32*1024, nullable:true)             // 32k
        headerSQL(maxSize: 32*1024, nullable:true)          // 32k
    }

    static mapping = {
        table name: "RPT_RESULT_DATA"
        crossTabHeader column: "CROSS_TAB_SQL", sqlType: DbUtil.longStringType
        value column: "VALUE", sqlType: DbUtil.longBlobType
        reportSQL column: "REPORT_SQL", sqlType: DbUtil.longStringType
        querySQL column: "QUERY_SQL", sqlType: DbUtil.longStringType
        versionSQL column: "VERSION_SQL", sqlType: DbUtil.longStringType
        gttSQL column: "GTT_SQL", sqlType: DbUtil.longStringType
        headerSQL column: "HEADER_SQL", sqlType: DbUtil.longStringType
    }

    public boolean isGzippedValue() {
        return value[0] == (byte) GZIPInputStream.GZIP_MAGIC && value[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8)
    }
}
