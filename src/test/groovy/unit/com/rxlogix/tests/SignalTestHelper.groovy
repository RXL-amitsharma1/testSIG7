package com.rxlogix.tests

import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority

import com.rxlogix.user.User

/**
 * Created by Lei Gao on 2/5/16.
 */
trait SignalTestHelper {

    def createTestData() {
        def admin = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User')
        def sharedUser = new User(username: 'drno', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Dr. No')
        def importer = new User(username: 'importer', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Interface User')
        admin.save()
        sharedUser.save()
        importer.save()

        assert (User.count == 3)


        def priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3)
        priority.save()
        assert (Priority.count == 1)


        def pvsState = new PVSState(value: "New", displayName: "New")
        pvsState.save()
        assert (PVSState.count == 1)

    }
}