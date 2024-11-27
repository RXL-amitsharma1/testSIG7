package com.rxlogix.util

import com.rxlogix.Constants
import grails.util.Holders
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.hibernate.Session

import javax.sql.DataSource
import java.sql.Connection

class DbUtil {

    // Strings < 65k
    static String getStringType() {
        forDialect("text", "clob")

    }

    // String < 16MB
    static String getMediumStringType() {
        forDialect("mediumtext", "clob")
    }

    // String < 4GB
    static String getLongStringType() {
        forDialect("longtext", "clob")
    }


    // Binary data < 16k
    static String getBlobType() {
        return forDialect("blob","blob")
    }

    // Binary data < 16MB
    static String getMediumBlobType() {
        return forDialect("mediumblob","blob")
    }


    // Binary data < 4GB
    static String getLongBlobType() {
        return forDialect("longblob","blob")

    }


    static String getDialectBlobType() {
        return forDialect("longblob", "blob")
    }


    private static String forDialect(String mySqlType, String oracleType) {
        def dialect = Holders.config?.dataSources?.dataSource?.dialect ?: 'org.hibernate.dialect.Oracle12cDialect'
        switch (dialect) {
            case "org.hibernate.dialect.MySQL5InnoDBDialect":
                return mySqlType
                break

            case "org.hibernate.dialect.Oracle10gDialect":
            case "org.hibernate.dialect.Oracle12cDialect":
                return oracleType
                break
        }
    }

    static void executePIIProcCall(Sql sql, String piOwner, String piEncryptionKey) {
        String procedureCall = "CALL P_SET_CONTEXT(?, ?)"
        try {
            if (Objects.nonNull(sql) && StringUtils.isNotBlank(piOwner) && Objects.nonNull(piEncryptionKey)) {
                sql.call(procedureCall, [piOwner, piEncryptionKey])
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    static void piiPolicy(Session session) {
        if (Objects.nonNull(session)) {
            try {
                session.createSQLQuery("CALL P_SET_CONTEXT(:param1, :param2)")
                        .setParameter("param1", Constants.PII_OWNER)
                        .setParameter("param2", Constants.PII_ENCRYPTION_KEY)
                        .executeUpdate()
                session.flush()
                session.clear()
            } catch (Exception ex) {
//                ex.printStackTrace()
            }
        }
    }

    static Sql getConnectionPool(Object dataSourceOrConnection) {
        Sql sql = null

        try {
            if (dataSourceOrConnection instanceof DataSource) {
                // If it's a DataSource, create Sql instance using it
                sql = new Sql(dataSourceOrConnection)
            } else if (dataSourceOrConnection instanceof Connection) {
                // If it's a Connection, create Sql instance using it
                sql = new Sql(dataSourceOrConnection)
            } else {
                throw new IllegalArgumentException("Invalid dataSourceOrConnection parameter")
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (sql != null) {
                sql.close()
            }
        }
        return sql
    }

}
