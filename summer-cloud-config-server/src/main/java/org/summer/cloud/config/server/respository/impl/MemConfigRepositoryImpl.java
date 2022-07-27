package org.summer.cloud.config.server.respository.impl;

import org.springframework.stereotype.Repository;
import org.summer.cloud.config.server.entity.ConfigEntity;
import org.summer.cloud.config.server.respository.ConfigRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * save the Config in mem
 *
 * @author shenfeng
 */
@Repository
public class MemConfigRepositoryImpl implements ConfigRepository {
    private final Map<String, ConfigEntity> configEntityMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> versionMap = new ConcurrentHashMap<>();

    @Override
    public String getConfig(String fileName) {
        ConfigEntity configEntity = configEntityMap.get(fileName);
        if (configEntity == null) {
            return "";
        }
        return configEntity.getConfig();
    }

    @Override
    public void save(String fileName, String config) {
        int newVersion = getNewVersion(fileName);
        ConfigEntity configEntity = ConfigEntity.builder().config(config).version(newVersion).build();
        configEntityMap.put(fileName, configEntity);
    }


    @Override
    public int getVersion(String fileName) {
        ConfigEntity configEntity = configEntityMap.get(fileName);
        if (configEntity == null) {
            return -1;
        }
        return configEntity.getVersion();
    }

    private int getNewVersion(String fileName) {
        AtomicInteger version = versionMap.get(fileName);
        if (version == null) {
            versionMap.putIfAbsent(fileName, new AtomicInteger(0));
            version = versionMap.get(fileName);
        }
        return version.incrementAndGet();
    }
}
