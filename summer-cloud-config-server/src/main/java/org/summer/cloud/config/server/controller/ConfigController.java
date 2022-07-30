package org.summer.cloud.config.server.controller;

import org.springframework.web.bind.annotation.*;
import org.summer.cloud.common.entity.ConfigEntity;
import org.summer.cloud.config.server.respository.ConfigRepository;

import javax.annotation.Resource;


/**
 * @author shenfeng
 */
@RestController
public class ConfigController {

    @Resource
    private ConfigRepository configRepository;

    @GetMapping("/config/{fileName}")
    public ConfigEntity config(@PathVariable String fileName) {
        return configRepository.getConfig(fileName);
    }

    @GetMapping("/version/{fileName}")
    public int version(@PathVariable String fileName) {
        return configRepository.getVersion(fileName);
    }

    @PostMapping("config/{fileName}")
    public String config(@PathVariable String fileName, @RequestBody String config) {
        configRepository.save(fileName, config);
        return "OK";
    }

}
