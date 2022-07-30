package org.summer.cloud.config.server.respository.impl;

import org.springframework.stereotype.Repository;
import org.summer.cloud.common.entity.ConfigEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.summer.cloud.config.server.respository.ConfigRepository;


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
    public ConfigEntity getConfig(String fileName) {
        return configEntityMap.get(fileName);
    }

    @Override
    public void save(String fileName, String config) {
        ConfigEntity configEntity = ConfigEntity.builder()
                .config(config)
                .version(getNewVersion(fileName))
                .fileName(fileName)
                .build();
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
