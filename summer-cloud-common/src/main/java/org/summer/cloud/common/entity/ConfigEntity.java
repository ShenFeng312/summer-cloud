package org.summer.cloud.common.entity;

import lombok.*;

/**
 * @author shenfeng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConfigEntity {
    private int version;
    private String config;
    private String fileName;
}
