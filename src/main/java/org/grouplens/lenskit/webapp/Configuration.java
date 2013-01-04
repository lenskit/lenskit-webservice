package org.grouplens.lenskit.webapp;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.grouplens.lenskit.core.Builder;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.params.meta.Parameters;
import org.grouplens.lenskit.util.PrimitiveUtils;


/**
 * <p>
 * Configuration handles the loading and access of a configuration file used by
 * the Dao and RecommendationEngine factories. The configuration file is a
 * properties file with the standard formating:
 * 
 * <pre>
 * # comment
 * blah=foo
 * blah2=bar
 * foo.bar=foobar
 * </pre>
 * 
 * </p>
 * <p>
 * Configuration defines one property within the config file:
 * "dao.factory". If present in the config file, it is
 * assumed that it contains the full class name of a {@link DataAccessObjectManager}. If not present, the
 * Configuration uses a {@link HibernateLenskitDaoFactory} by default.
 * </p>
 * 
 * @author Michael Ludwig
 * @author Daniel Kluver
 */
public class Configuration {
    public static final String LENSKIT_FACTORY_PROPERTY_NAME = "lenskit.factory";
    
    private final Properties properties;
    
    /**
     * Load the properties file from the given input stream. After loading,
     * the properties within the Configuration may be modified by calling
     * {@link #setProperty(String, String)}.
     * 
     * @param configFile input stream of the properties file
     * @throws ConfigurationException if there is any problem loading file
     */
    public Configuration(String configFile) throws ConfigurationException {
        if (configFile == null)
            throw new ConfigurationException("configuration file name cannot be null");
        properties = new Properties();
        try {
            properties.load(new FileReader(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        	throw new ConfigurationException("Error loading or parsing properties");
        }
    }

    /**
     * Create and return a new instance of DataAccessObjectManager using the class type
     * specified in the "factory.dao" property. If the property does not exist,
     * a {@link HibernateLenskitDaoFactory} is created.
     * 
     * @return A DataAccessObjectManager
     * @throws ConfigurationException if a DataAccessObjectManager could not be created
     */
    public LenskitRecommenderEngineFactory getLenskitRecommenderEngineFactory() {
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(this.getDaoFactory());
        for (Entry<String, String> property : this.getProperties().entrySet()) {
        	configureComponent(property.getKey(), property.getValue(), factory);
        }
        return factory;
    }
    
    public DAOFactory getDaoFactory() {
    	return newInstance(DAOFactory.class, "rec.dao.factory", "org.grouplens.lenskit.webapp.sql.JDBCRatingServerDAOFactory");
    }

    /**
     * Return the value of the property with the given <tt>name</tt>. Keep in
     * mind that the specified name must be the final property name, after group
     * names have been combined. If the property does not exist within this
     * Configuration, null is returned.
     * 
     * @param name The final name of the property
     * @return The String value of the property or null
     * @throws NullPointerException if name is null
     */
    public String getProperty(String name) {
        if (name == null)
            throw new NullPointerException("Name cannot be null");
        return properties.getProperty(name);
    }

    /**
     * Return the value of the property <tt>name</tt>. If the property does not
     * exist, then <tt>dflt</tt> is automatically returned.
     * 
     * @param name The final name of the property
     * @param dflt The value to return if <tt>name</tt> is not a property
     * @return The property value or <tt>dflt</tt>
     * @throws NullPointerException if name is null
     */
    public String getProperty(String name, String dflt) {
        if (name == null)
            throw new NullPointerException("Name cannot be null");
        return properties.getProperty(name, dflt);
    }

    /**
     * <p>
     * Modify this Configuration to add, modify or remove a property. If
     * <tt>value</tt> is null, the property with the given <tt>name</tt> will be
     * removed from this Configuration. Otherwise subsequent calls to
     * {@link #getProperty(String)} with <tt>name</tt> will return
     * <tt>value</tt>.
     * </p>
     * <p>
     * This can be used to override parts of the Configuration under special
     * circumstances while still relying on the factories to provide a
     * {@link Dao} and {@link RecommendationEngine}.
     * </p>
     * 
     * @param name The final name of the property to assign
     * @param value The new value of the property, or null if it's to be removed
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is the empty string
     */
    public void setProperty(String name, String value) {
        if (name == null)
            throw new NullPointerException("Name cannot be null");
        if (name.isEmpty())
            throw new IllegalArgumentException("Name cannot be the empty string");
        
        if (value != null)
            properties.setProperty(name, value);
        else
            properties.remove(name);
    }

    /**
     * Return a Map of all currently assigned properties in this Configuration.
     * The returned map can be mutated without affecting the Configuraiton, and
     * it will not reflect changes to the Configuration object.
     * 
     * @return Map of all properties
     */
    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<Object, Object> p: properties.entrySet()) {
            map.put((String) p.getKey(), (String) p.getValue());
        }
        return map;
    }
    
    /*
     * This assumes that type is either RecommendationFactory or DataAccessObjectManager,
     * because it looks for a constructor that takes a single Configuration.
     */
    @SuppressWarnings("unchecked")
    private <T> T newInstance(Class<T> type, String name, String dfltValue) {
        String value = getProperty(name, dfltValue);
        
        Class<?> pType;
        T retVal;
        try {
            pType = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("The class " + value + " could not be found", name);
        }
        if (!type.isAssignableFrom(pType))
            throw new ConfigurationException("The property " + name + " must be a class name that is an instance of " 
                                             + type + ", not: " + pType);
        try {
            retVal =  (T) pType.getConstructor(Configuration.class).newInstance(this);
            return retVal;
        } catch (NoSuchMethodException e) {
        	try {
        		retVal = (T) pType.getConstructor().newInstance();
        		return retVal;
        	} catch (Exception e2) {
        		throw new ConfigurationException("An instance of " + value + " could not be instanitated");
        	}
        } catch (Exception e) {
            e.printStackTrace();
        	throw new ConfigurationException("An instance of " + value + " could not be instantiated", e);
        }
    }
    
    public class ConfigurationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public ConfigurationException(String msg) {
            super(msg);
        }

        public ConfigurationException(String propName, String msg) {
            super(msg + (propName == null ? "" : " - " + propName));
        }
        
        public ConfigurationException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void configureComponent(String key, String value, LenskitRecommenderEngineFactory factory) {
        if (!key.startsWith(LENSKIT_FACTORY_PROPERTY_NAME))
            return;
        
        key = "org.grouplens.lenskit" + key.substring(LENSKIT_FACTORY_PROPERTY_NAME.length());
        
        try {
            Class<?> keyClass = Class.forName(key);
            if (keyClass.isAnnotation()) {
                Class<? extends Annotation> annotKey = (Class<? extends Annotation>) keyClass;
                if (Parameters.isParameter(annotKey)) {
                    Class<?> paramType = Parameters.getParameterType(annotKey);
                    
                    if (Number.class.isAssignableFrom(paramType)) {
                        // set the parameter to an actual number
                        factory.set(annotKey, PrimitiveUtils.parse((Class<? extends Number>) paramType, value));
                    } else {
                        // value should be a regular class
                        Class valueClass = Class.forName(value);
                        if (Builder.class.isAssignableFrom(valueClass))
                            factory.setBuilder(annotKey, paramType, valueClass);
                        else
                            factory.setComponent(annotKey, paramType, valueClass);
                    }
                }
            } else {
                // regular binding between class and implementation
                Class valueClass = Class.forName(value);
                if (Builder.class.isAssignableFrom(valueClass))
                    factory.setBuilder(keyClass, valueClass);
                else
                    factory.setComponent(keyClass, valueClass);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public <T> T getInstance(Class<T> clazz, String defaultPath) {
    	for (Entry<String, String> e : getProperties().entrySet()) {
    		try {
    			if (clazz.isAssignableFrom(Class.forName(e.getValue()))) {
        			return newInstance(clazz, e.getValue(), defaultPath);
    			}
    		} catch (Exception exc) {
    			continue;
    		}
    	}
    	return null;
    }
}

