package org.summer.cloud.config.server.respository;

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
    String getConfig(String fileName);

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
