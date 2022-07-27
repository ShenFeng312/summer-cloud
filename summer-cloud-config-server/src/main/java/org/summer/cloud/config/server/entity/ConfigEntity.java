package org.summer.cloud.config.server.entity;

import lombok.Builder;
import lombok.Getter;

/**
 * @author shenfeng
 */
@Getter
@Builder
public class ConfigEntity {
    int version;
    String config;
}
