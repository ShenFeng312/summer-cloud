package org.summer.cloud.common.configdemo;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BeanA {
    @Resource
    private BeanB beanB;
}
