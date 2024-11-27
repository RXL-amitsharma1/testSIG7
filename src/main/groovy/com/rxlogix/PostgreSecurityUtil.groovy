package com.rxlogix

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
@Transactional
class PostgreSecurityUtil {

    GrailsApplication grailsApplication
    DataSource dataSource

    void updateColumnsAccess() {
        Map<String, List<String>> sensitiveData = grailsApplication.config.postgresql.sensitive.data as Map<String, List<String>>
        Connection connection = null
            PreparedStatement preparedStatement = null
            ResultSet resultSet = null

            try {
                connection = dataSource.getConnection()
                String roleQuery = "SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pvsignal_user')";
                preparedStatement = connection.prepareStatement(roleQuery)
                resultSet = preparedStatement.executeQuery()
                if (resultSet.next() && resultSet.getBoolean(1)) {
                    for (String key in sensitiveData.keySet()) {
                        String selectColumnsSQL = "select column_name FROM information_schema.columns WHERE table_schema = '" + connection.getSchema() + "' AND table_name   = '" + key + "';"
                        preparedStatement = connection.prepareStatement(selectColumnsSQL)
                        resultSet = preparedStatement.executeQuery()
                        List<String> columns = new ArrayList<>()
                        while (resultSet.next()) {
                            columns.add(resultSet.getString("column_name"))
                        }
                        columns.removeAll(sensitiveData.get(key))
                        if (columns.isEmpty()) {
                            continue
                        }
                        String accessibleColumns = "(";
                        for (String column in columns) {
                            accessibleColumns += column
                            if (columns.indexOf(column) < columns.size() - 1) {
                                accessibleColumns += ", "
                            } else {
                                accessibleColumns += ")"
                            }
                        }
                        println "Revoking select grant from " + key
                        String revokeSQL = "REVOKE SELECT ON TABLE " + key + " FROM pvsignal_user"

                        println "Granting select grant on " + key + " for columns " + accessibleColumns
                        String grantSQL = "GRANT SELECT " + accessibleColumns + " ON TABLE " + key + " TO pvsignal_user"

                        preparedStatement = connection.prepareStatement(revokeSQL)
                        preparedStatement.execute()

                        preparedStatement = connection.prepareStatement(grantSQL)
                        preparedStatement.execute()
                    }
                }
            } finally {
                resultSet?.close()
                preparedStatement?.close()
                connection?.close()
            }

    }

}
