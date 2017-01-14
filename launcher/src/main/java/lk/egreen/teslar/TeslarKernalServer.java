package lk.egreen.teslar;

import lk.egreen.teslar.deployer.DeploymentEngine;
import lk.egreen.teslar.deployer.PlatformDeploymentManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dewmal on 1/13/17.
 */
public class TeslarKernalServer {

    /**
     * A directory along the classpath where we expect to find base OSGi
     * bundles.
     */
    private static final String OSGI_BUNDLE_DIR = "osgi";
    private static final String OSGI_BUNDLE_SYS_PROPERTY = "osgiPluginsList";

    /**
     * A directory along the classpath where we expect to find Teslar plugins.
     */
    private static final String PLATFORM_BUNDLE_DIR = "platform";
    private static final String PLATFORM_BUNDLE_SYS_PROPERTY = "platformPluginsList";

    public static void main(String[] args) {
        new TeslarKernalServer().start();
    }

    private void start() {
        Map<String, String> props = new HashMap<String, String>();

        // Start with a clean bundle cache.
        props.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        // Set the cache directory path.
        props.put(Constants.FRAMEWORK_STORAGE, getCacheDir().getAbsolutePath());



        // Bundles should have the extension classloader as their parent classloader.
        props.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_EXT);

        //props.put("felix.log.level","4");

        // Add all system properties that seem to be OSGi property names.
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("osgi.") || key.startsWith("org.osgi.")) {
                props.put(key, System.getProperty(key));
            }
        }

        Framework framework = null;

        // Iterate over frameworks on the classpath to see if one matches. See the
        // Javadoc for org.osgi.framework.launch.FrameworkFactory for information
        // about using ServiceLoader to find the available framework implementations.
        for (FrameworkFactory factory : ServiceLoader.load(FrameworkFactory.class)) {
            framework = factory.newFramework(props);
        }

        if (framework == null) {
            throw new RuntimeException("Cannot find an OSGi framework");
        }
        try {
            framework.start();

        } catch (BundleException e) {
            throw new RuntimeException("Cannot start OSGi framework", e);
        }

        BundleContext bc = framework.getBundleContext();


        startOSGIBundles(bc);
        startPlatformBundles(bc);
    }

    /**
     * Substitutes expression.
     *
     * @param expression - the string.
     * @param evars      - environment variables.
     * @return the substitute string.
     */
    private static String substitute(String expression, Properties evars) {
        assert evars != null && expression != null;
        final String regex = "([^\\)]*)(%\\()([\\w]+)(\\))"; // four groups
        final Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String evarReplacement = evars.getProperty(m.group(3));
            if (evarReplacement == null) {
                System.err.println(String.format("Expression: %s substituting empty string because evar %s is undefined", expression, m.group(3)));
                evarReplacement = "";
            }
            String beforeReplacement = m.group(1);
            sb.append(beforeReplacement + evarReplacement);
            expression = expression.substring(m.end());
            m = pattern.matcher(expression);
        }
        return sb.toString() + expression;
    }

    private static File getCacheDir() {
        String filePath = substitute(TeslarProperties.DEFAULT_TESLAR_PROPERTIES.getProperty("cacheDir"), System.getProperties());

        // Want to make sure that any old bundles are cleared out.
        File cacheDir = new File(filePath);

        if (cacheDir.exists()) {
            if (!deleteDir(cacheDir)) {
                System.err.println("Could not delete OSGi cache directory");
            }
        }

        // (Re)create the cache directory.
        if (!cacheDir.mkdirs()) {
            throw new RuntimeException("Could not create osgi cache dir (" + cacheDir
                    + "). Ensure that the directory is writable and executable.");
        }

        return cacheDir;
    }

    private static boolean deleteDir(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                if (!deleteDir(child)) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return f.delete();
    }


    public void startPlatformBundles(BundleContext bc) {
        DeploymentEngine.Deploy(bc,new DirectoryBundlesListTracker(), PLATFORM_BUNDLE_SYS_PROPERTY,
                PLATFORM_BUNDLE_DIR, true, Collections.<String>emptySet());
    }

    private void startOSGIBundles(BundleContext bc) {
//        String OSGI_BUNDLE_DIR = TeslarProperties.DEFAULT_TESLAR_PROPERTIES.getProperty(TeslarKernalServer.OSGI_BUNDLE_DIR);

        DeploymentEngine.Deploy(bc,new DirectoryBundlesListTracker(), OSGI_BUNDLE_SYS_PROPERTY,
                OSGI_BUNDLE_DIR, true, Collections.<String>emptySet());
    }





}
