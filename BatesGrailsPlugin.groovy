class BatesGrailsPlugin {
    // the plugin version
    def version = "0.34"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"

    // the other plugins this plugin depends on
    def dependsOn = [mongodbMorphia: "0.8.2"]
//	executor: "0.3"]

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Business Audit Trail Events System (BATES) Plugin" // Headline display name of the plugin
    def author = "Michael Salera"
    def authorEmail = "mikesalera@mac.com"
    def description = '''\
Business Audit Trail Events System (BATES) is designed to take the load off the primary relational database (GORM dataSource) and allow fine-grained log events, 
that is, changes to the records or documents themselves, and create these logs in MongoDB.  It is intended to be used across the enterprise, esp. if your 
Mongo cluster is setup supporting sharding by Department or Application area/domain.

Primary reason for writing Bates was not only to use the MongoDB backend, but also to provide similar features to Shawn Hartsock's excellent audit-logging plug-in,
which was not upgraded to work with Grails 2.0 or above.  Though this may be done in a future release....

Bates's job is to make audit trail simple, unobtrusive, yet powerful enough to be able to reconstruct should things go awry or front-end developers would like to 
build an Admin style front end to view and filter thru all log documents, for example.

(c) 2012 Mike Salera / FRC
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/bates"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Free Range Consultants", url: "http://www.freerangeconsultants.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Johanan Lancaon", email: "javageek@gmail.com" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]


//METHODS
    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

def doWithSpring = {
   businessAuditLogService(com.freerangeconsultants.plugins.bates.core.BusinessAuditLogService)

   auditListener(com.freerangeconsultants.plugins.bates.listeners.BusinessAuditLogListener)
   hibernateEventListeners(org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners) {
      listenerMap = ['post-insert': auditListener,
                     'pre-update': auditListener,
                     'pre-delete': auditListener]
   }
}

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

/*
    def doWithApplicationContext = { applicationContext ->
        application.mainContext.eventTriggeringInterceptor.datastores.each { k, datastore ->
    	    applicationContext.addApplicationListener(new com.freerangeconsultants.plugins.bates.listeners.BusinessAuditLogListener(datastore))
        }
    }
*/

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
