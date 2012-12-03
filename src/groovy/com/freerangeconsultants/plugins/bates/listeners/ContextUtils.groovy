
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import javax.servlet.http.HttpServletRequest

class ContextUtils {

  def static getBeanFromApplicationContext(String beanName){
    ApplicationContext ctx = (ApplicationContext)ApplicationHolder.getApplication().getMainContext()
    def bean
    try{
      bean = ctx.getBean(beanName)
    } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException ex){
      //do nothing. this just means the requested bean doesn't exist and the method will return null
    }
    return bean
  }
}