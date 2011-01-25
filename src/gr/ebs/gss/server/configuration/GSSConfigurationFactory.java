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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton that loads properties from gss.properties and
 * makes them available to all
 *
 * @author droutsis
 */
public final class GSSConfigurationFactory {

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(GSSConfigurationFactory.class);

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
	 * @return the configuration object
	 */
	public synchronized static DataConfiguration getConfiguration() {
		try {
			if (configuration==null) {
				PropertiesConfiguration gssConfig = (PropertiesConfiguration) getClass(PropertiesConfiguration.class.getCanonicalName()).newInstance();
				gssConfig.setBasePath("");
				gssConfig.setFileName(configFilename);
				gssConfig.setEncoding("ISO-8859-7");
				// Set automatic reloading.
				gssConfig.setReloadingStrategy(new VfsFileChangedReloadingStrategy());
				gssConfig.load();
				// Decorator.
				configuration = new DataConfiguration(gssConfig);
			}
			return configuration;
		}
		catch (ClassNotFoundException e) {
			// Use empty configuration, so we get no NPE but default values
			configuration = new DataConfiguration(new BaseConfiguration());
			logger.error("Error in ExternalAPI initialization! GSS is running with default values!", e);
			return configuration;
		} catch (InstantiationException e) {
			// Use empty configuration, so we get no NPE but default values
			configuration = new DataConfiguration(new BaseConfiguration());
			logger.error("Error in ExternalAPI initialization! GSS is running with default values!", e);
			return configuration;
		} catch (IllegalAccessException e) {
			// Use empty configuration, so we get no NPE but default values
			configuration = new DataConfiguration(new BaseConfiguration());
			logger.error("Error in ExternalAPI initialization! GSS is running with default values!", e);
			return configuration;
		} catch (ConfigurationException e) {
			// Use empty configuration, so we get no NPE but default values
			configuration = new DataConfiguration(new BaseConfiguration());
			logger.error("Error in ExternalAPI initialization! GSS is running with default values!", e);
			return configuration;
		}
	}

	/**
	 * Extends the FileChangedReloadingStrategy from Commons Configuration, adding
	 * support for files in JBoss MC VFS.
	 *
	 * @author past
	 */
	private static class VfsFileChangedReloadingStrategy extends FileChangedReloadingStrategy {
	    /** Constant for the jar URL protocol.*/
	    private static final String JAR_PROTOCOL = "jar";
	    /** Constant for the JBoss MC VFSFile URL protocol.*/
	    private static final String VFSFILE_PROTOCOL = "vfsfile";

	    @Override
		protected File getFile()
	    {
	        return configuration.getURL() != null ? fileFromURL(configuration
	                .getURL()) : configuration.getFile();
	    }

	    /**
	     * Helper method for transforming a URL into a file object. This method
	     * handles file: and jar: URLs, as well as JBoss VFS-specific vfsfile:
	     * URLs.
	     *
	     * @param url the URL to be converted
	     * @return the resulting file or <b>null </b>
	     */
	    private File fileFromURL(URL url)
	    {
	        if (VFSFILE_PROTOCOL.equals(url.getProtocol()))
	        {
	            String path = url.getPath();
	            try
	            {
	                return ConfigurationUtils.fileFromURL(new URL("file:" + path));
	            }
	            catch (MalformedURLException mex)
	            {
	                return null;
	            }
	        }
	        else if (JAR_PROTOCOL.equals(url.getProtocol()))
	        {
	            String path = url.getPath();
	            try
	            {
	                return ConfigurationUtils.fileFromURL(new URL(path.substring(0,
	                        path.indexOf('!'))));
	            }
	            catch (MalformedURLException mex)
	            {
	                return null;
	            }
	        } else
				return ConfigurationUtils.fileFromURL(url);
	    }
	}
}
