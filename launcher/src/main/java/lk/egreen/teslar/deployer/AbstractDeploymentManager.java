package lk.egreen.teslar.deployer;

import lk.egreen.teslar.DirectoryBundlesListTracker;
import lk.egreen.teslar.TeslarProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by dewmal on 1/14/17.
 */
public abstract class AbstractDeploymentManager {







    List<String> getBundleList(DirectoryBundlesListTracker bundleListTracker, String bundleListSysProperty,
                                       String bundleListPath, boolean isBundleListPathIsFolder, Collection<String> excludeBundles) {
        String propertyVal = TeslarProperties.DEFAULT_TESLAR_PROPERTIES.getProperty(bundleListSysProperty);
        if (propertyVal != null) {

            String[] bundles = null;
            String pathPrefix = "";
            if (isBundleListPathIsFolder) {
                pathPrefix = propertyVal + "/";
                File folderPath = new File(propertyVal);

                bundles = folderPath.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });

            } else {
                bundles = propertyVal.split("[ \\t]*,[ \\t]*");
            }

            for (String bundlePath : bundles) {
                if (!excludeBundles.contains(bundlePath)) {
                    bundleListTracker.addBundle(pathPrefix + bundlePath);
                }
            }
        }

        List<String> bundlesLoc = bundleListTracker.getBundlesLocation();
        if (bundlesLoc == null || bundlesLoc.isEmpty()) {
            URL bundleDirURL = getClass().getClassLoader().getResource(bundleListPath);
            if (bundleDirURL == null) {
                System.err.println("Bundle directory not found, skipped: " + bundleListPath);
                return Collections.emptyList();
            }

            File bundleDir;
            try {
                bundleDir = new File(bundleDirURL.toURI());
            } catch (URISyntaxException e) {
                System.err.println("Error getting path to bundle directory " + bundleListPath);
                return Collections.emptyList();
            }

            if (!bundleDir.isDirectory()) {
                System.err.println("Bundle directory path not a directory: " + bundleListPath);
                return Collections.emptyList();
            }

            File[] bundles = bundleDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            for (File bundleFile : bundles) {
                if (!excludeBundles.contains(bundleFile.getName())) {
                    bundleListTracker.addBundle(bundleFile.getAbsolutePath());
                }
            }
        }

        return bundleListTracker.getBundlesLocation();
    }

    void loadBundles(List<String> bundlesList, BundleContext bc) {
        List<Bundle> bundles = new ArrayList<Bundle>();

        for (String bundlePath : bundlesList) {
            File bundleFile = new File(bundlePath);
            if (!bundleFile.exists()) {
                throw new RuntimeException("Plugin path not found: " + bundlePath);
            } else if (!bundleFile.canRead()) {
                throw new RuntimeException("Cannot read plugin file: " + bundlePath);
            } else {
                Bundle b = loadBundle(bundleFile, bc);
                if (b != null) {
                    bundles.add(b);
                }
            }
        }

        startBundles(bundles);
    }

    Bundle loadBundle(File bundleFile, BundleContext bc) {
        String bundleURL = null;
        try {
            if (bundleFile.isDirectory()) {
                bundleURL = "reference:" + bundleFile.toURI().toURL().toExternalForm();
            } else {
                bundleURL = bundleFile.toURI().toURL().toExternalForm();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad path to plugin bundle: " + bundleFile.getAbsolutePath());
        }

        try {
            Bundle b = bc.installBundle(URLDecoder.decode(bundleURL, "UTF8"));
            return b;
        } catch (Exception e) {
            throw new RuntimeException("Error installing bundle from " + bundleURL, e);
        }
    }

    /**
     * Starts an ordered list of bundles.
     *
     * @param bundles the list of bundles to start
     */
    private void startBundles(List<Bundle> bundles) {
        for (Bundle bundle : bundles) {
            if (!isFragment(bundle)) {
                try {
                    bundle.start();
                } catch (BundleException ex) {
                    throw new RuntimeException("Error starting bundle " + bundle.getLocation(), ex);
                }
            }
        }
    }


    private boolean isFragment(Bundle bundle) {
        return (bundle.getHeaders().get("Fragment-Host") != null);
    }
}
