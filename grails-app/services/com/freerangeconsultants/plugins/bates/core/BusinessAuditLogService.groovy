package com.freerangeconsultants.plugins.bates.core


import com.freerangeconsultants.plugins.bates.domain.AuditLogEvent

/**
 * Log audit events from any persistence object to MongoDB sharded cluster
 *
 * @version 1.0.34
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
   * @return   com.google.code.morphia.query.MorphiaIterator to navigate query results
   */
  def findAllLogEventsByClassAndId(Class clazz, persistedObjectId) {
    if (clazz && persistedObjectId) {
      return AuditLogEvent.findAll(['className': clazz.name, 'objectId': persistedObjectId])
    }
    else {
      return Collections.emptyList()
    }
  }


  /**
   * This method used transparently from the audit( ) method injected into Domain classes
   * where their static auditable = true
   *
   * This method sends an instance of AuditLogEvent to MongoDB
   * assumes Morphia classes are mapped
   *
   * @param eventType
   * @param className          this.class.simpleName
   * @param persistedObjectId  id of the Entity being saved
   * @param oldeState          former state of the object (EventType.PreUpdate events only)
   * @param newState           new or current state of the object
   * @return true on success
   */
  def recordLogEvent(String eventType, String className, persistedObjectId, oldeState, newState) {
    def auditEvent = new AuditLogEvent(eventName: eventType, className: className, objectId: persistedObjectId as String)
    if (oldeState) { auditEvent.oldState = oldeState }
    if (newState) { auditEvent.newState = newState }

	try {
		auditEvent.validate()
		println( "${eventType}  for class ${className}  [Id] -> ${persistedObjectId}" )
		return auditEvent.save(flush: true)
	}
	catch (Exception ex) {	
		System.err.println(auditEvent)
		System.err.println("Caught ${ex.message}")
		ex.printStackTrace()
	}
	false
  }
}
