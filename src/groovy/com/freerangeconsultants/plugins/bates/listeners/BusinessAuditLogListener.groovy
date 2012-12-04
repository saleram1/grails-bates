package com.freerangeconsultants.plugins.bates.listeners

import com.freerangeconsultants.plugins.bates.core.BusinessAuditLogService

import grails.util.GrailsUtil
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.HibernateException
import org.hibernate.cfg.Configuration
import org.hibernate.collection.PersistentList
import org.hibernate.collection.PersistentSet
import org.hibernate.event.*
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext

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
//class BusinessAuditLogListener extends AbstractPersistenceEventListener {


/**
 * Or you could use the low-level Hibernate Events...
 *  Complete list of listeners/events can be obtained at <tt>org.hibernate.event.EventListeners</tt>.
 *
 * @see org.hibernate.event.EventListeners
 * @author Mike Salera, Shawn Hartsock
 *
 */
class BusinessAuditLogListener
  implements PostInsertEventListener, PreUpdateEventListener, PreDeleteEventListener, Initializable {

  def Log LOG = LogFactory.getLog(BusinessAuditLogListener.class)

  /**
   * Log insertions made to the current model in the Audit Trail.
   * new objects
   *
   * @param event
   */
  @Override
  public void onPostInsert(PostInsertEvent event) {
    handleEvent('onPostInsert', event)
  }

  /**
   * Log updates made to the current model in the Audit Trail.
   * Return true if the operation should be vetoed
   * @param event
   */
  @Override
  public boolean onPreUpdate(PreUpdateEvent event) {
    handleEvent('onPreUpdate', event)
    return false
  }

  /**
   * Log deletions made to the current model in the the Audit Trail.
   * Return true if the operation should be vetoed
   * @param event
   */
  @Override
  public boolean onPreDelete(PreDeleteEvent event) {
    handleEvent('onPreDelete', event)
    return false
  }

  def handleEvent(String eventName, event) {
  LOG.info("handleEvent: ${eventName}")

  def businessAuditLogService = ContextUtils.getBeanFromApplicationContext("businessAuditLogService")

    def String className = null
    def Object persistedObjectId = null
    def oldState = null // for UPDATES
    def newState = null

    //skip if test mode
    if (!GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_TEST) && isAuditableEntity(event?.entity)) {
        className = event?.entity?.class.name
        persistedObjectId = event?.entity?.id?.toString()

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

  @Override
  public void initialize(Configuration cfg) {
  LOG.info("initializing")
  }


  def Boolean isAuditableEntity(entity) {
    entity?.metaClass.hasProperty(entity, 'auditable') && entity?.auditable == Boolean.TRUE
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
      if (value != null && !(value instanceof PersistentSet) && !(value instanceof PersistentList)) {
        aMap[key] = value?.toString()
      }
      else { entriesToBeRemoved << key }
    }
    entriesToBeRemoved.each { key -> aMap.remove(key) }
    return aMap
  }
}


class ContextUtils {

  def static getBeanFromApplicationContext(String beanName){
    ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext() as ApplicationContext

    def bean = null
    try{
      bean = ctx.getBean(beanName)
    } catch (NoSuchBeanDefinitionException ex){
    }
    return bean
  }
}
