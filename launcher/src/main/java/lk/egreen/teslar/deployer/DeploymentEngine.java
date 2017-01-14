package lk.egreen.teslar.deployer;

import lk.egreen.teslar.DirectoryBundlesListTracker;
import org.osgi.framework.BundleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by dewmal on 1/14/17.
 */
public class DeploymentEngine {


    public static void Deploy(BundleContext bundleContext, DirectoryBundlesListTracker bundleListTracker, String bundleListSysProperty,
                              String bundleListPath, boolean isBundleListPathIsFolder, Collection<String> excludeBundles) {

        AbstractDeploymentManager deploymentManager = new OSGIDeploymentManager();

        List<String> bundleList = deploymentManager.getBundleList(new DirectoryBundlesListTracker(), bundleListSysProperty,
                bundleListPath, true, Collections.<String>emptySet());
        deploymentManager.loadBundles(bundleList,bundleContext);
    }


}
