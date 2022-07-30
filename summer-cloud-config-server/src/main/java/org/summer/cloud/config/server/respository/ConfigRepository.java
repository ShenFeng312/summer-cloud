package org.summer.cloud.config.server.respository;


import org.summer.cloud.common.entity.ConfigEntity;

/**
 * @author shenfeng
 */
public interface ConfigRepository {
    /**
     * getConfigWithFileName
     *
     * @param fileName fileName
     * @return config
     */
    ConfigEntity getConfig(String fileName);

    /**
     * save config
     *
     * @param fileName fileName
     * @param config   config
     */
    void save(String fileName, String config);

    /**
     * get the config version by fileName
     *
     * @param fileName fileName
     * @return config version
     */
    int getVersion(String fileName);
}
