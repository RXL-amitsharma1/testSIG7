package com.rxlogix.cache

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IList

class HazelcastService {
    static transactional = false

    HazelcastInstance hazelcastInstance
    def grailsApplication

    Map getCache(String cacheName) {
        hazelcastInstance.getMap(cacheName)
    }

    List getList(String name) {
        this.hazelcastInstance.getList(name)
    }

    Set getSet(String name) {
        this.hazelcastInstance.getSet(name)
    }

    String getName() {
        this.hazelcastInstance.getName()
    }
}
