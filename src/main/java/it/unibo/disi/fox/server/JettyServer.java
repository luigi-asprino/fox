package it.unibo.disi.fox.server;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import it.unibo.disi.fox.resources.IRISService;

public class JettyServer {

	private static Logger logger = LoggerFactory.getLogger(JettyServer.class);

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException, ConfigurationException {

		ModelFactory.createDefaultModel();

		Configurations configs = new Configurations();
		Configuration config;
		if (args.length >= 1) {
			logger.info("Configuration file {}", args[0]);
			config = configs.properties(args[0]);
		} else {
			logger.info("Configuration file config.properties");
			config = configs.properties("config.properties");
		}

		int port = config.getInt("port", 8080);

		// Jetty server
		Server jettyServer = new Server(port);

		String resourcePackage = config.getString("resourcePackage", "it.unibo.disi.fox.resources");

		// Main context handler
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");

		// Add the filter, and then use the provided FilterHolder to configure it
		// Enable cross site requests
		FilterHolder cors = servletContextHandler.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");

		ServletHolder servletHolder = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", resourcePackage);
		servletHolder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
		servletHolder.setInitParameter("jersey.config.server.wadl.disableWadl", "true");
		servletHolder.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.jackson.JacksonFeature");

		logger.info(" will be available at " + "http://localhost:" + port + "/*");

		// add main context to Jetty
		jettyServer.setHandler(servletContextHandler);

		try {
			IRISService.init(config.getString("dataset"), config.getString("classAttribute"));
			jettyServer.start();
			logger.info("Done! Jetty Server is up and running!");
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				jettyServer.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
