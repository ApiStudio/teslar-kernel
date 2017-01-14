package lk.egreen.teslar.engine;

import lk.egreen.teslar.engine.core.config.Configuration;

/**
 * Created by dewmal on 1/13/17.
 */
public interface RunTime {

    <T extends Configuration> T getConfiguration();

}
