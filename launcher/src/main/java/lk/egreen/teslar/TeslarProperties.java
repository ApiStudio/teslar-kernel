package lk.egreen.teslar;

/**
 * Created by dewmal on 1/14/17.
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * get the MCT system wide properties
 */
@SuppressWarnings("serial")
public class TeslarProperties extends Properties {
    public static final TeslarProperties DEFAULT_TESLAR_PROPERTIES;

    static {
        try {
            DEFAULT_TESLAR_PROPERTIES = new TeslarProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TeslarProperties(String propertyFileName) throws IOException {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(propertyFileName);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + propertyFileName);
        load(is);
    }

    public TeslarProperties() throws IOException {
        String defaultProperties = "properties/teslar.properties";

        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(defaultProperties);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + defaultProperties);
        load(is);
    }

    public void load(InputStream is) throws IOException {
        try {
            super.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore exception
            }
        }
    }

    /*
     * get a set of properties named by your class
     */
    public TeslarProperties(Class<?> c) throws IOException {

        String className = getClassName(c);
        String pkgProperties = "properties/" + className + ".properties";

        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(pkgProperties);

        if (is == null)
            throw new IOException("MCT properties util:: Unable to get mct property file: " + pkgProperties);
        load(is);
    }



    /*
     * returns the class (without the package if any)
     */

    private String getClassName(Class<?> c) {
        String FQClassName = c.getName();
        int firstChar;
        firstChar = FQClassName.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            FQClassName = FQClassName.substring(firstChar);
        }
        return FQClassName;
    }

    // Normally we could
    @Override
    public String getProperty(String key) {
        String value = System.getProperty(key);
        if (value != null) {
            return trim(value);
        } else {
            return trim(super.getProperty(key));
        }
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            return trim(value);
        } else {
            return trim(super.getProperty(key,defaultValue));
        }
    }

    /**
     * Return a string value trimmed of leading and trailing spaces, or null if the
     * string is null.
     *
     * @param s the string to trim
     * @return the trimmed value, or null
     */
    protected static String trim(String s) {
        if (s == null) {
            return null;
        } else {
            return s.trim();
        }
    }

}
