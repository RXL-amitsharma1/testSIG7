package com.rxlogix;

import org.grails.datastore.gorm.events.AutoTimestampEventListener;
import org.grails.datastore.mapping.engine.ModificationTrackingEntityAccess;
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor;
import org.hibernate.Hibernate;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityMetamodel;


import java.util.Map;


/**
 * Listens for Hibernate events and publishes corresponding Datastore events.
 * <p>
 * Custom extension to handle NPE bug for old state being null for detached entities.
 */
public class CustomClosureEventTriggeringInterceptor extends ClosureEventTriggeringInterceptor {

    private void publishEvent(AbstractEvent hibernateEvent, AbstractPersistenceEvent mappingEvent) {
        mappingEvent.setNativeEvent(hibernateEvent);
        if (eventPublisher != null) {
            eventPublisher.publishEvent(mappingEvent);
        }
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent hibernateEvent) {
        Object entity = hibernateEvent.getEntity();
        Class type = Hibernate.getClass(entity);
        MappingContext mappingContext = datastore.getMappingContext();
        PersistentEntity persistentEntity = mappingContext.getPersistentEntity(type.getName());
        AbstractPersistenceEvent grailsEvent;
        ModificationTrackingEntityAccess entityAccess = null;
        if (persistentEntity != null) {
            entityAccess = new ModificationTrackingEntityAccess(mappingContext.createEntityAccess(persistentEntity, entity));
            grailsEvent = new org.grails.datastore.mapping.engine.event.PreUpdateEvent(this.datastore, persistentEntity, entityAccess);
        } else {
            grailsEvent = new org.grails.datastore.mapping.engine.event.PreUpdateEvent(this.datastore, entity);
        }

        publishEvent(hibernateEvent, grailsEvent);
        boolean cancelled = grailsEvent.isCancelled();
        if (!cancelled && entityAccess != null) {
            synchronizeHibernateState(hibernateEvent, entityAccess);
        }
        return cancelled;

    }

    private void synchronizeHibernateState(PreUpdateEvent hibernateEvent, ModificationTrackingEntityAccess entityAccess) {
        Map<String, Object> modifiedProperties = getModifiedPropertiesWithAutotimestamp(hibernateEvent, entityAccess);

        if (!modifiedProperties.isEmpty()) {
            Object[] state = hibernateEvent.getState();
            EntityPersister persister = hibernateEvent.getPersister();
            synchronizeHibernateState(persister, state, modifiedProperties);
        }
    }

    private void synchronizeHibernateState(EntityPersister persister, Object[] state, Map<String, Object> modifiedProperties) {
        EntityMetamodel entityMetamodel = persister.getEntityMetamodel();
        for (Map.Entry<String, Object> entry : modifiedProperties.entrySet()) {
            Integer index = entityMetamodel.getPropertyIndexOrNull(entry.getKey());
            if (index != null) {
                state[index] = entry.getValue();
            }
        }
    }

    /**
     * @param hibernateEvent
     * @param entityAccess
     * @return
     */
    private Map<String, Object> getModifiedPropertiesWithAutotimestamp(PreUpdateEvent hibernateEvent, ModificationTrackingEntityAccess entityAccess) {
        Map<String, Object> modifiedProperties = entityAccess.getModifiedProperties();

        EntityMetamodel entityMetamodel = hibernateEvent.getPersister().getEntityMetamodel();
        Integer dateCreatedIdx = entityMetamodel.getPropertyIndexOrNull(AutoTimestampEventListener.DATE_CREATED_PROPERTY);

        Object[] oldState = hibernateEvent.getOldState();
        Object[] state = hibernateEvent.getState();

        // Only for "dateCreated" property, "lastUpdated" is handled correctly
        // Fix for GORM 6.1.x bug https://github.com/grails/gorm-hibernate5/issues/114
        if (dateCreatedIdx != null && oldState != null && oldState[dateCreatedIdx] != null && !oldState[dateCreatedIdx].equals(state[dateCreatedIdx])) {
            modifiedProperties.put(AutoTimestampEventListener.DATE_CREATED_PROPERTY, oldState[dateCreatedIdx]);
        }

        return modifiedProperties;
    }
}