package com.freerangeconsultants.plugins.bates.listeners

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.EntityAccess;
import org.grails.datastore.mapping.engine.event.*;
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.model.PersistentEntity;
import org.springframework.context.ApplicationEvent

import com.freerangeconsultants.plugins.bates.core.BusinessAuditLogService


/**
 * As of Grails 2.0 there is a new API for plugins and applications to register and listen for persistence events.
 * This API is not tied to Hibernate and also works for other persistence plugins such as 
 * the MongoDB plugin for GORM.
 *
 * To use this API you need to subclass and implement a single method called onPersistenceEvent.
 *
 *  @author Mike Salera
 *  @version 1.0
 */
class BusinessAuditLogListener extends AbstractPersistenceEventListener {

//     BusinessAuditLogService businessAuditLogService
     Log LOG = LogFactory.getLog(BusinessAuditLogListener.class)


     public BusinessAuditLogListener(final Datastore dataStore) { 
     	     super(dataStore)
	     println this.class.name
	     println "Running on: " + dataStore.class.name

     }

     @Override
     public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
             return true
     }

@Override
protected void onPersistenceEvent(final AbstractPersistenceEvent event) {
//    handleEvent(event)
    switch(event.eventType) {
        case EventType.PostInsert:
	     println ''
	     println this.class.name + "  Got a Persistence event!"
            println "POST INSERT ${event.entityObject}"
        break

        case EventType.PreUpdate:
	     println ''
	     println this.class.name + "  Got a Persistence event!"
            println "POST UPDATE ${event.entityObject}"
        break;

        case EventType.PreDelete:
     	     println ''	
	     println this.class.name + "  Got a Persistence event!"
            println "POST DELETE ${event.entityObject}"
	    
    }
}

  def handleEvent(final AbstractPersistenceEvent event) {
    def businessAuditLogService = getBeanFromApplicationContext("businessAuditLogService")

    def String className = null
    def Object persistedObjectId = null
    def oldState = null // for UPDATES
    def newState = null

    //skip if test mode
    if (!GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_TEST) && isAuditableEntity(event?.entityObject)) {
        className = event?.entityObject?.class.name
        persistedObjectId = event?.entityObject?.id?.toString()

// switch event.eventType
        if (eventName == 'onPostInsert') {
          newState = getStateMap(event?.persister?.propertyNames, event?.getState())
          businessAuditLogService.recordLogEvent('insert', className, persistedObjectId, null, newState)
        }
        else if (eventName == 'onPreUpdate') {
          oldState = getStateMap(event?.persister?.propertyNames, event?.getOldState())
          newState = getStateMap(event?.persister?.propertyNames, event?.getState())
          businessAuditLogService.recordLogEvent('update', className, persistedObjectId, oldState, newState)
        }
        else if (eventName == 'onPreDelete') {
          oldState = getStateMap(event?.persister?.propertyNames, event?.getDeletedState())
          businessAuditLogService.recordLogEvent('delete', className, persistedObjectId, oldState, null)
        }
        else {
          throw new IllegalArgumentException("Cannot support event: ${eventName}")
        }
    }
  }


  Boolean isAuditableEntity(entity) {
    entity?.metaClass.hasProperty(entity, 'auditable') && entity.auditable == Boolean.TRUE
  }


  private def getStateMap(String[] names, Object[] state) {
    def map = [:]
    for (int i = 0; i < names.length; i++) {
      if (names[i]) {
        map[names[i]] = state[i]
      }
    }
    sanitizeMap(map)
  }


  // propertyNames may have null values on the above call to put
  //create Map with no missing teeth
  private def sanitizeMap(aMap) {
    def entriesToBeRemoved = []
    aMap?.each { key, value ->
      if (key && value) {
        aMap[key] = value?.toString()
      }
      else { entriesToBeRemoved << key }
    }
    entriesToBeRemoved.each { key -> aMap.remove(key) }
    return aMap
  }

}