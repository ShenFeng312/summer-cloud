package org.summer.cloud.config.refresh;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author shenfeng
 */
@Slf4j
public class SummerCloudRefreshScope implements Scope, BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor,
        DisposableBean, ApplicationListener<CloudSummerEnvRefreshEvent>, Ordered {

    private static final String SCOPE = "refresh";

    private final Map<String, BeanLifecycleWrapper> cache = new ConcurrentHashMap<>();

    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    @Override
    public void destroy() {
        cache.forEach((k, v) -> {
            v.destroy();
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope(SCOPE, this);
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        BeanLifecycleWrapper value = new BeanLifecycleWrapper(name, objectFactory);
        this.cache.put(name, value);
        this.locks.putIfAbsent(name, new ReentrantReadWriteLock());

        return value.getBean();

    }

    @Override
    public Object remove(String name) {
        BeanLifecycleWrapper value = this.cache.remove(name);
        if (value == null) {
            return null;
        }

        return value.getBean();
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        BeanLifecycleWrapper value = this.cache.get(name);
        if (value == null) {
            return;
        }
        value.setDestroyCallback(callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return SCOPE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String name : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(name);

            if (SCOPE.equals(definition.getScope())) {
                BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, name);
                BeanDefinitionHolder proxy = ScopedProxyUtils.createScopedProxy(holder, registry, true);

                if (registry.containsBeanDefinition(proxy.getBeanName())) {
                    registry.removeBeanDefinition(proxy.getBeanName());
                }
                BeanDefinition beanDefinition = proxy.getBeanDefinition();
                if (beanDefinition instanceof RootBeanDefinition) {
                    RootBeanDefinition root = (RootBeanDefinition) beanDefinition;
                    if (root.getDecoratedDefinition() != null && root.hasBeanClass()
                            && root.getBeanClass() == ScopedProxyFactoryBean.class) {
                        if (SCOPE.equals(root.getDecoratedDefinition().getBeanDefinition().getScope())) {
                            root.setBeanClass(LockedScopedProxyFactoryBean.class);
                            root.getConstructorArgumentValues().addGenericArgumentValue(this);
                            root.setSynthetic(true);
                        }
                    }
                }
                registry.registerBeanDefinition(proxy.getBeanName(), beanDefinition);
            }


        }

    }


    @Override
    public void onApplicationEvent(CloudSummerEnvRefreshEvent event) {
        cache.forEach((k, v) -> {
            v.destroy();
            v.getBean();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }


    public static class LockedScopedProxyFactoryBean extends ScopedProxyFactoryBean
            implements MethodInterceptor {

        private final SummerCloudRefreshScope scope;

        private String targetBeanName;

        public LockedScopedProxyFactoryBean(SummerCloudRefreshScope scope) {
            this.scope = scope;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            super.setBeanFactory(beanFactory);
            Object proxy = getObject();
            if (proxy instanceof Advised) {
                Advised advised = (Advised) proxy;
                advised.addAdvice(0, this);
            }
        }

        @Override
        public void setTargetBeanName(String targetBeanName) {
            super.setTargetBeanName(targetBeanName);
            this.targetBeanName = targetBeanName;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            if (AopUtils.isEqualsMethod(method) || AopUtils.isToStringMethod(method)
                    || AopUtils.isHashCodeMethod(method) || isScopedObjectGetTargetObject(method)) {
                return invocation.proceed();
            }
            Object proxy = getObject();
            ReadWriteLock readWriteLock = this.scope.getLock(this.targetBeanName);
            if (readWriteLock == null) {
                if (log.isDebugEnabled()) {
                    log.debug("For bean with name [" + this.targetBeanName
                            + "] there is no read write lock. Will create a new one to avoid NPE");
                }
                readWriteLock = new ReentrantReadWriteLock();
            }
            Lock lock = readWriteLock.readLock();
            lock.lock();
            try {
                if (proxy instanceof Advised) {
                    Advised advised = (Advised) proxy;
                    ReflectionUtils.makeAccessible(method);
                    return ReflectionUtils.invokeMethod(method, advised.getTargetSource().getTarget(),
                            invocation.getArguments());
                }
                return invocation.proceed();
            } catch (UndeclaredThrowableException e) {
                throw e.getUndeclaredThrowable();
            } finally {
                lock.unlock();
            }
        }


        private boolean isScopedObjectGetTargetObject(Method method) {
            return method.getDeclaringClass().equals(ScopedObject.class) && "getTargetObject".equals(method.getName())
                    && method.getParameterTypes().length == 0;
        }

    }


    private ReadWriteLock getLock(String targetBeanName) {
        return locks.get(targetBeanName);
    }

}
