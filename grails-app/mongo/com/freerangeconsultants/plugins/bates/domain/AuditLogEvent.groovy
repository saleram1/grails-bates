package com.freerangeconsultants.plugins.bates.domain

import com.google.code.morphia.annotations.Entity
import com.google.code.morphia.annotations.Id
import groovy.transform.ToString

/**
 * Audit Log Event
 * One of these documents is stored in MongoDB shard for each record-level change detected
 * these can be from PostInsert, PostUpdate, or PostDelete Grails system events.
 *
 * Developed custom for the B.A.T.E.S. business audit trail plugin for Grails 2.1.x or above
 * @version 1.0
 * @author Mike Salera
 */

@ToString
@Entity(value="audits", noClassnameStored=true)
class AuditLogEvent implements Serializable, Comparable {

  @Id
  String uuid = UUID.randomUUID()
  String application = "bates-plugin"
  String region = "US"
  String eventName
  String className
  String objectId
  Map<String, String> oldState
  Map<String, String> newState
  Date dateCreated = new Date()


//METHODS
  def getDiffMap() {
    def ignoreList = ['version', 'lastUpdated']
    StringBuilder sb = new StringBuilder();
    if (newState && oldState) {
      newState.each { key, value ->
        if (value && value != oldState[key] && !(key in ignoreList))
          sb.append("${key} = ${value}, was, ${key} = ${oldState[key]}  ")
      }
    }
    sb.toString()
  }

  int compareTo(java.lang.Object anObject) { return id.compareTo(anObject.id) }

  static constraints = {
    eventName(nullable: false)
    className(nullable: false)
    objectId(nullable: false)
    oldState(nullable: true)
    newState(nullable: true)
  }
}
