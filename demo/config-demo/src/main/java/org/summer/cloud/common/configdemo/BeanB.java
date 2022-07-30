package org.summer.cloud.common.configdemo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("refresh")
@Getter
public class BeanB {
    @Value("${aa}")
    private String aa;

}
