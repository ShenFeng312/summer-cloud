package org.summer.cloud.rpc.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.summer.cloud.rpc.annotation.SummerCloudResource;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shenfeng
 */
@Slf4j
public class SummerCloudResourceBeanDefinitionPostProcessor implements BeanFactoryPostProcessor,
		InstantiationAwareBeanPostProcessor, ApplicationContextAware {

	private final Set<Class<?>> classSet = new HashSet<>();

	private BeanDefinitionRegistry beanDefinitionRegistry;

	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


	private ApplicationContext app;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			Class<?> type = beanFactory.getType(beanDefinitionName);
			ReflectionUtils.doWithFields(type, field -> {
				SummerCloudResource annotation = field.getAnnotation(SummerCloudResource.class);
				if (annotation != null && !classSet.contains(field.getType())) {
					RootBeanDefinition beanDefinition = new RootBeanDefinition();
					beanDefinition.setAttribute("interface", field.getType());
					beanDefinition.setBeanClass(HttpProxyConsumerFactory.class);
					beanDefinition.setAttribute("host", annotation.host());
					GenericBeanDefinition targetDefinition = new GenericBeanDefinition();
					targetDefinition.setBeanClass(field.getType());
					String beanName = new DefaultBeanNameGenerator().generateBeanName(beanDefinition,
							beanDefinitionRegistry);
					beanDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, beanName + "_decorated"));
					beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
					classSet.add(field.getType());
				}
			});
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.app = applicationContext;
		this.beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		InjectionMetadata metadata = findSummerCloudResourceMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (BeanCreationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
		}
		return pvs;
	}

	private InjectionMetadata findSummerCloudResourceMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
		// Fall back to class name as cache key, for backwards compatibility with custom callers.
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					metadata = buildSummerCloudResourceMetadata(clazz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildSummerCloudResourceMetadata(Class<?> clazz) {
		if (!AnnotationUtils.isCandidateClass(clazz, SummerCloudResource.class)) {
			return InjectionMetadata.EMPTY;
		}

		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				MergedAnnotation<?> ann = findSummerCloudResourceAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (log.isInfoEnabled()) {
							log.info("Autowired annotation is not supported on static fields: " + field);
						}
						return;
					}
					//					boolean required = determineRequiredStatus(ann);
					currElements.add(new SummerCloudResourceBeanDefinitionPostProcessor.AutowiredFieldElement(field, true));
				}
			});

			//			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
			//				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
			//				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
			//					return;
			//				}
			//				MergedAnnotation<?> ann = findSummerCloudResourceAnnotation(bridgedMethod);
			//				if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
			//					if (Modifier.isStatic(method.getModifiers())) {
			////						if (logger.isInfoEnabled()) {
			////							logger.info("Autowired annotation is not supported on static methods: " + method);
			////						}
			//						return;
			//					}
			//					if (method.getParameterCount() == 0) {
			////						if (logger.isInfoEnabled()) {
			////							logger.info("Autowired annotation should only be used on methods with parameters: " +
			////									method);
			////						}
			//					}
			////					boolean required = determineRequiredStatus(ann);
			//					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
			//					currElements.add(new AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement(method, true, pd));
			//				}
			//			});

			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return InjectionMetadata.forElements(elements, clazz);
	}

	private MergedAnnotation<?> findSummerCloudResourceAnnotation(AccessibleObject ao) {
		MergedAnnotations annotations = MergedAnnotations.from(ao);
		MergedAnnotation<?> annotation = annotations.get(SummerCloudResource.class);
		if (annotation.isPresent()) {
			return annotation;
		}
		return null;
	}

	private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.app != null && this.app.containsBean(autowiredBeanName)) {
					((ConfigurableListableBeanFactory) this.app.getAutowireCapableBeanFactory()).registerDependentBean(autowiredBeanName, beanName);
				}
				if (log.isTraceEnabled()) {
					log.trace("Autowiring by type from bean name '" + beanName +
							"' to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}

	@Nullable
	private Object resolvedCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
			Assert.state(this.app != null, "No BeanFactory available");
			return this.app.getAutowireCapableBeanFactory().resolveDependency(descriptor, beanName, null, null);
		} else {
			return cachedArgument;
		}
	}


	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached;

		@Nullable
		private volatile Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value;
			if (this.cached) {
				try {
					value = resolvedCachedArgument(beanName, this.cachedFieldValue);
				} catch (NoSuchBeanDefinitionException ex) {
					// Unexpected removal of target bean for cached argument -> re-resolve
					value = resolveFieldValue(field, bean, beanName);
				}
			} else {
				value = resolveFieldValue(field, bean, beanName);
			}
			if (value != null) {
				ReflectionUtils.makeAccessible(field);
				field.set(bean, value);
			}
		}

		@Nullable
		private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
			DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
			desc.setContainingClass(bean.getClass());
			Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
			Assert.state(app != null, "No BeanFactory available");
			TypeConverter typeConverter =
					((ConfigurableListableBeanFactory) app.getAutowireCapableBeanFactory()).getTypeConverter();
			Object value;
			try {
				value = app.getAutowireCapableBeanFactory().resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
			} catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
			}
			synchronized (this) {
				if (!this.cached) {
					Object cachedFieldValue = null;
					if (value != null || this.required) {
						cachedFieldValue = desc;
						registerDependentBeans(beanName, autowiredBeanNames);
						if (autowiredBeanNames.size() == 1) {
							String autowiredBeanName = autowiredBeanNames.iterator().next();
							if (app.containsBean(autowiredBeanName) &&
									app.isTypeMatch(autowiredBeanName, field.getType())) {
								cachedFieldValue = new SummerCloudResourceBeanDefinitionPostProcessor.ShortcutDependencyDescriptor(
										desc, autowiredBeanName, field.getType());
							}
						}
					}
					this.cachedFieldValue = cachedFieldValue;
					this.cached = true;
				}
			}
			return value;
		}
	}

	private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

		private final String shortcut;

		private final Class<?> requiredType;

		public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType) {
			super(original);
			this.shortcut = shortcut;
			this.requiredType = requiredType;
		}

		@Override
		public Object resolveShortcut(BeanFactory beanFactory) {
			return beanFactory.getBean(this.shortcut, this.requiredType);
		}
	}


}
