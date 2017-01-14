package lk.egreen.teslar;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dewmal on 1/14/17.
 */
public class DirectoryBundlesListTracker {
    private List<String> bundleLocations = new LinkedList<String>();

    public void addBundle(String bundleLoc) {
        bundleLocations.add(bundleLoc);
    }

    public List<String> getBundlesLocation() {
        if (bundleLocations.isEmpty()) {
            return Collections.emptyList();
        }
        return bundleLocations;
    }

}
