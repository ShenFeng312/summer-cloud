package org.summer.cloud.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.config.ConfigDataResource;

/**
 * @author shenfeng
 */
@Getter
@Setter
@EqualsAndHashCode
public class    SummerCloudConfigDataResource extends ConfigDataResource {
    private String fileName;
}
