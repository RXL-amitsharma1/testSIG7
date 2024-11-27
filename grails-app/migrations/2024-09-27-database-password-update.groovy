databaseChangeLog = {
    changeSet(id: '5846846851684-0001', author: 'nikhil (generated)') {
        preConditions(onFail: 'MARK_RAN') {
            // Ensure the PVUSER table exists
            tableExists(tableName: 'PVUSER')
        }

        // Update LDAP users with empty password
        update(tableName: 'PVUSER') {
            column(name: 'password', value: '{noop}')
            where("user_type = 'LDAP' AND (password = '' OR password IS NULL)")
        }

        // Update NON_LDAP users with the current value prepended by {bcrypt}
        sql("""
            UPDATE PVUSER
            SET password = CONCAT('{bcrypt}', password)
            WHERE user_type = 'NON_LDAP' AND password IS NOT NULL AND password NOT LIKE '{bcrypt}%'
        """)
    }
}
