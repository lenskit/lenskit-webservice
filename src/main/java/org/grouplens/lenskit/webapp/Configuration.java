package org.grouplens.lenskit.webapp;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.webapp.sql.JDBCRatingServerDAOFactory;


/**
 * <p>
 * Configuration handles the loading and access of a configuration file used by
 * the DAO and LenskitRecommenderEngine factories. The configuration file is a
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
 * "rec.dao.factory". If present in the config file, it is
 * assumed that it contains the full class name of a {@link DAOFactory}. If not present, the
 * Configuration uses a {@link JDBCRatingServerDAOFactory} by default.
 * </p>
 * 
 * @author Michael Ludwig
 * @author Daniel Kluver
 */
public class Configuration {
    private static final String LENSKIT_FACTORY_PROPERTY_NAME = "lenskit.factory";
    
    private static final String WITHIN_SEPERATOR = "/";
    
    private final Properties properties;
    
    /**
     * Load the properties file from the given file name. After loading,
     * the properties within the Configuration may be modified by calling
     * {@link #setProperty(String, String)}.
     * 
     * @param configFile Path for the properties file
     * @throws ConfigurationException if there is any problem loading file
     */
    public Configuration(String configFile) throws ConfigurationException {
        if (configFile == null) {
            throw new ConfigurationException("configuration file name cannot be null");
        }
        properties = new Properties();
        try {
            properties.load(new FileReader(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        	throw new ConfigurationException("Error loading or parsing properties");
        }
    }

    /**
     * Create, configure, and return a new instance of {@link LenskitRecommenderEngineFactory},
     * using the specified {@link DAOFactory} and components if possible.
     * @return A {@link LenskitRecommenderEngineFactory}
     */
    public LenskitRecommenderEngineFactory getLenskitRecommenderEngineFactory() {
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(this.getDaoFactory());
        for (Entry<String, String> property : this.getProperties().entrySet()) {
        	configureComponent(property.getKey(), property.getValue(), factory);
        }
        return factory;
    }
    
    /**
     * Create and return a new instance of {@link DAOFactory} using the type
     * specified in the "rec.dao.factory" property. If the property does not exist,
     * a {@link JDBCRatingServerDAOFactory} is created.
     * 
     * @return A {@link DataAccessObjectManager}
     * @throws ConfigurationException if a {@link DAOFactory} could not be created
     */
    public DAOFactory getDaoFactory() {
    	return newInstance(DAOFactory.class, "rec.dao.factory",
    			"org.grouplens.lenskit.webapp.sql.JDBCRatingServerDAOFactory");
    }

    /**
     * Return the value of the property with the given name. Keep in
     * mind that the specified name must be the final property name, after group
     * names have been combined. If the property does not exist within this
     * Configuration, <tt>null</tt> is returned.
     * 
     * @param name The final name of the property
     * @return The value of the property or <tt>null</tt>
     * @throws NullPointerException if <tt>name</tt> is null
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        return properties.getProperty(name);
    }

    /**
     * Return the value of the specified property If the property does not
     * exist, then <tt>dflt</tt> is automatically returned.
     * 
     * @param name The final name of the property
     * @param dflt The value to return if <tt>name</tt> is not a property
     * @return The property value or <tt>dflt</tt>
     * @throws NullPointerException if name is null
     */
    public String getProperty(String name, String dflt) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        return properties.getProperty(name, dflt);
    }

    /**
     * <p>
     * Modify this Configuration to add, modify or remove a property. If
     * <tt>value</tt> is null, the property with the given name will be
     * removed from this Configuration. Otherwise subsequent calls to
     * {@link #getProperty(String)} with <tt>name</tt> will return
     * <tt>value</tt>.
     * </p>
     * <p>
     * This can be used to override parts of the Configuration under special
     * circumstances while still relying on the factories to provide a
     * {@link DataAccessObject} and {@link RecommenderEngine}.
     * </p>
     * 
     * @param name The final name of the property to assign
     * @param value The new value of the property, or <tt>null</tt> if it's to be removed
     * @throws NullPointerException if name is <tt>null</tt>
     * @throws IllegalArgumentException if <tt>name</tt> is the empty string
     */
    public void setProperty(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        } else if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be the empty string");
        }
        if (value != null)
            properties.setProperty(name, value);
        else
            properties.remove(name);
    }

    /**
     * Return a Map of all currently assigned properties in this Configuration.
     * The returned map can be mutated without affecting the Configuration, and
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
    
    // This looks for a constructor with a Configuration as its only argument
    @SuppressWarnings("unchecked")
    private <T> T newInstance(Class<T> type, String name, String defaultPath) {
        T retVal;
    	String value = getProperty(name, defaultPath);
    	
        
        Class<?> pType;
        try {
            pType = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("The class " + value + " could not be found", name);
        }
        if (!type.isAssignableFrom(pType)) {
            throw new ConfigurationException("The property " + name + " must be a class name that is an instance of " 
                                             + type + ", not: " + pType);
        }
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
        // Ignore property if it doesn't match the appropriate pattern
    	if (!key.startsWith(LENSKIT_FACTORY_PROPERTY_NAME)) {
            return;
    	}
        
        key = "org.grouplens.lenskit" + key.substring(LENSKIT_FACTORY_PROPERTY_NAME.length());
        try {
            Class<?> contextClass = null;
            Class<?> keyClass;
        	
        	// Binding within a context
        	if (key.contains(WITHIN_SEPERATOR)) {
            	contextClass = Class.forName(key.substring(0, key.indexOf(WITHIN_SEPERATOR)));
            	keyClass = Class.forName(key.substring(key.indexOf(WITHIN_SEPERATOR)+1));
            } else {
            	keyClass = Class.forName(key);
            }
            
            if (keyClass.isAnnotation()) {
                Class<? extends Annotation> annotKey = (Class<? extends Annotation>) keyClass;
                Parameter param = annotKey.getAnnotation(Parameter.class);
                if (param != null) {
                    Class<?> paramType = param.value();                  
                    if (Number.class.isAssignableFrom(paramType)) {
                        // set the parameter to an actual number
                        factory.set(annotKey).to(parseNumber((Class<? extends Number>) paramType, value));
                    } else if (Enum.class.isAssignableFrom(paramType)) {
                    	// set parameter to enum value
                    	factory.set(annotKey).to(Enum.valueOf((Class<Enum>)paramType, value));
                    } else if (String.class.isAssignableFrom(paramType)) {
                    	// set parameter directly to value
                    	factory.set(annotKey).to(value);
                    }
                }
            } else {
                // regular binding between class and implementation or provider
                Class valueClass = Class.forName(value);
                if (contextClass != null) {
                	factory.within(contextClass).bind(keyClass).to(valueClass);
                } else {
                	factory.bind(keyClass).to(valueClass);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create a new instance of the desired class, as specified in the Configuration.
     * @param clazz The type of the instance.
     * @param defaultPath The name of the default type to instantiate.
     * @return A new instance of the the desired type.
     */
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
    
    // Utility method for parsing
    private static Number parseNumber(Class<? extends Number> type, String value) {
        if (type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Byte.class)) {
            return Byte.parseByte(value);
        } else if (type.equals(Short.class)) {
            return Short.parseShort(value);
        } else {
            throw new IllegalArgumentException("Unsupported Number type: " + type);
        }
    }
}
