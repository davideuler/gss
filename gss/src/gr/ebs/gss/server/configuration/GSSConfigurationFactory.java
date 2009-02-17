/*
 * Copyright 2006, 2007, 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.server.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * Singleton that loads properties from gss.properties and
 * makes them available to all
 *
 * @author droutsis
 */
public final class GSSConfigurationFactory {

	/**
	 * The filename of the file containing system properties.
	 */
	private static String configFilename = "gss.properties";

	/**
	 * The (single) configuration object
	 */
	private static DataConfiguration configuration = null;

	/**
	 * Exists only to defeat instantiation.
	 */
	private GSSConfigurationFactory() {
	}

	/**
	 * Because multiple classloaders are commonly used in many situations -
	 * including servlet containers - you can wind up with multiple singleton instances
	 * no matter how carefully you've implemented your singleton classes.
	 * If you want to make sure the same classloader loads your singletons,
	 * you must specify the classloader yourself; for example:
	 *
	 * @param classname
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Class getClass(String classname) throws ClassNotFoundException {
	      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	      if(classLoader == null)
	         classLoader = GSSConfigurationFactory.class.getClassLoader();
	      return classLoader.loadClass(classname);
	}

	/**
	 * It returns the configuration object
	 * @return Configuration
	 * @throws ConfigurationException
	 *
	 */
	public synchronized static DataConfiguration getConfiguration() throws ConfigurationException {
		try {
			if (configuration==null) {
				PropertiesConfiguration gssConfig = (PropertiesConfiguration) getClass(PropertiesConfiguration.class.getCanonicalName()).newInstance();
				gssConfig.setBasePath("");
				gssConfig.setFileName(configFilename);
				gssConfig.setEncoding("ISO-8859-7");
				// Set automatic reloading
				gssConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
				gssConfig.load();
				// Decorator
				configuration = new DataConfiguration(gssConfig);
			}
			return configuration;
		}
		catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		} catch (InstantiationException e) {
			throw new ConfigurationException(e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(e);
		}
	}

}
