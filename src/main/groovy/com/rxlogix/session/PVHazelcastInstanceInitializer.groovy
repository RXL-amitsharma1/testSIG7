package com.rxlogix.session

import com.hazelcast.config.Config
import com.hazelcast.config.JoinConfig
import com.hazelcast.config.MapAttributeConfig
import com.hazelcast.config.MapIndexConfig
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import grails.core.GrailsApplication
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.session.hazelcast.HazelcastSessionRepository
import org.springframework.session.hazelcast.PrincipalNameExtractor

class PVHazelcastInstanceInitializer extends AbstractFactoryBean<HazelcastInstance> {

    GrailsApplication grailsApplication

    ConfigObject hazelcastConfig

    SpringSessionConfigProperties springSessionConfigProperties

    HazelcastInstance startHazelcastServer() {
        String groupName = grailsApplication.config.hazelcast.group.name
        String groupPassword = grailsApplication.config.hazelcast.group.password
        int serverPort = grailsApplication.config.hazelcast.server.port
        boolean serverPortAutoIncrement = grailsApplication.config.hazelcast.server.auto.increment.port
        int serverPortCount = grailsApplication.config.hazelcast.server.portCount
        String serverOutboundPortDefinition = grailsApplication.config.hazelcast.server.outbound.port.definition
        String instanceName = grailsApplication.config.hazelcast.server.instance.name

        boolean enableManagementCenter = grailsApplication.config.hazelcast.management.center.enabled
        MapAttributeConfig attributeConfig = new MapAttributeConfig()
                .setName(HazelcastSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractor(PrincipalNameExtractor.class.getName())
        Config config = new Config()
        config.getMapConfig(springSessionConfigProperties.getMapName())
                .addMapAttributeConfig(attributeConfig)
                .addMapIndexConfig(new MapIndexConfig(
                        HazelcastSessionRepository.PRINCIPAL_NAME_ATTRIBUTE, false))
        config.setInstanceName(instanceName)
        config.getGroupConfig().setName(groupName).setPassword(groupPassword)

        if (enableManagementCenter) {
            String managementCenterUrl = hazelcastConfig.management.center.url
            int managementCenterUpdateInterval = hazelcastConfig.management.center.update.interval
            config.getManagementCenterConfig().setEnabled(enableManagementCenter)
            config.getManagementCenterConfig().setUrl(managementCenterUrl)
            config.getManagementCenterConfig().setUpdateInterval(managementCenterUpdateInterval)
        }

        NetworkConfig networkConfig = config.getNetworkConfig()
        networkConfig.setPort(serverPort).setPortAutoIncrement(serverPortAutoIncrement)
        networkConfig.getInterfaces().setEnabled(false)
        networkConfig.setPortCount(serverPortCount)
        networkConfig.addOutboundPortDefinition(serverOutboundPortDefinition)

        JoinConfig joinConfig = networkConfig.getJoin()
        joinConfig.getMulticastConfig().setEnabled(false)
        joinConfig.getAwsConfig().setEnabled(false)
        joinConfig.getTcpIpConfig().setEnabled(true)

        def nodes = grailsApplication.config.hazelcast.network.nodes
        nodes.each { String node ->
            joinConfig.getTcpIpConfig().addMember(node)
        }

        HazelcastInstance hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config)

        println("Hazelcast server : " + hazelcastInstance.name + " joined to cluster having " +
                hazelcastInstance.getCluster().getMembers().size() + " members.")
        return hazelcastInstance
    }

    @Override
    Class<?> getObjectType() {
        return HazelcastInstance
    }

    @Override
    protected HazelcastInstance createInstance() throws Exception {
        return startHazelcastServer()
    }

    @Override
    void destroy() throws Exception {
        println("Shutting down all hazelcast instances on this JVM...")
        Hazelcast.shutdownAll()
    }
}
