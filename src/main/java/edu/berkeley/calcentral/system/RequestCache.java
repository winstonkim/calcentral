package edu.berkeley.calcentral.system;

import org.jboss.resteasy.plugins.cache.server.ServletServerCache;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

public class RequestCache extends ServletServerCache {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(
				servletContextEvent.getServletContext());
		CacheWarmer warmer = (CacheWarmer) springContext.getBean("cacheWarmer");
		warmer.setCache(this);
	}

	public void remove(String uri) {
		this.cache.remove(uri);
	}

}
