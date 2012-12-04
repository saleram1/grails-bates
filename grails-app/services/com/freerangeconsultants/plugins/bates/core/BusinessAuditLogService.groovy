package com.freerangeconsultants.plugins.bates.core

import com.freerangeconsultants.plugins.bates.domain.AuditLogEvent

/**
 * Log audit events from any persistence object to MongoDB sharded cluster
 *
 * @version 1.0
 * @author Mike Salera
 */
class BusinessAuditLogService {

  static transactional = false


  //METHODS

  /**
   * Use this method to return a list of the History of the given object id
   *
   * @param clazz                 Class object
   * @param persistedObjectId     id of the object caller is looking for
   * @return
   */
  def findAllLogEventsByClassAndId(Class clazz, persistedObjectId) {
    return []
  }

  /**
   * This method used transparently from the audit( ) method injected into Domain classes
   * where their static auditable = true
   *
   * This method sends an instance of AuditLogEvent to MongoDB
   *
   * @param eventType
   * @param className          this.class.simpleName
   * @param persistedObjectId  id of the Entity being saved
   * @param oldeState          former state of the object (EventType.PreUpdate events only)
   * @param newState           new or current state of the object
   * @return true on success
   */
  def recordLogEvent(String eventType, String className, persistedObjectId, oldeState, newState) {
    //magic of the dataStore
    def auditEvent = new AuditLogEvent(eventName: eventType, className: className, persistedObjectId: persistedObjectId as String)
    if (oldeState) { auditEvent.oldState = oldeState }
    if (newState) { auditEvent.newState = newState }
    auditEvent.save(flush: true)
    println( "${eventType}  for class ${className}  Id -> ${persistedObjectId}" )
    print('.')
    true
  }
}
