package com.pttl.mybatis.datasource.forceconfigure;

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties.CoreConfiguration;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.pttl.mybatis.datasource.DynamicDataSources;
import com.pttl.mybatis.datasource.DynamicRoutingDataSource;

import jakarta.annotation.PostConstruct;

/**
 * 
 * @ClassName: MybatisAutoConfiguration
 * @Description: mybatis的配置 获取SqlSessionFactory
 * @author jackson.song
 * @mail suxuan696@gmail.com
 * @date 2020年6月3日
 *
 */
@Configuration
@Component
public class MybatisAutoConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(MybatisAutoConfiguration.class);

	private final MybatisProperties properties;

	private final Interceptor[] interceptors;

	private final ResourceLoader resourceLoader;

	private final DatabaseIdProvider databaseIdProvider;

	private final List<ConfigurationCustomizer> configurationCustomizers;
	DynamicRoutingDataSource dataSource = DynamicRoutingDataSource.getInstance();
	@Autowired
	private DynamicDataSources dataSources;

	public MybatisAutoConfiguration(MybatisProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider,
			ResourceLoader resourceLoader, ObjectProvider<DatabaseIdProvider> databaseIdProvider,
			ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
		this.properties = properties;
		this.interceptors = (Interceptor[]) interceptorsProvider.getIfAvailable();
		this.resourceLoader = resourceLoader;
		this.databaseIdProvider = (DatabaseIdProvider) databaseIdProvider.getIfAvailable();
		this.configurationCustomizers = (List<ConfigurationCustomizer>) configurationCustomizersProvider
				.getIfAvailable();
	}

	@PostConstruct
	public void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
			Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
			Assert.state(resource.exists(), "Cannot find config location: " + resource
					+ " (please add config file or check your Mybatis configuration)");
		}
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		dataSource.setDefaultTargetDataSource(this.dataSources.getDataSource().get("defaultDataSource"));
		dataSource.setTargetDataSources(this.dataSources.getDataSource());
		dataSource.afterPropertiesSet();
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource((DataSource) dataSource);
		factory.setVfs(SpringBootVFS.class);
		if (StringUtils.hasText(this.properties.getConfigLocation()))
			factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
		CoreConfiguration coreConfiguration = this.properties.getConfiguration();
		if (coreConfiguration == null && !StringUtils.hasText(this.properties.getConfigLocation()))
			coreConfiguration = new CoreConfiguration();
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		coreConfiguration.applyTo(configuration);
		if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers))
			for (ConfigurationCustomizer customizer : this.configurationCustomizers)
				customizer.customize(configuration);
		factory.setConfiguration(configuration);
		if (this.properties.getConfigurationProperties() != null)
			factory.setConfigurationProperties(this.properties.getConfigurationProperties());
		if (!ObjectUtils.isEmpty((Object[]) this.interceptors))
			factory.setPlugins(this.interceptors);
		if (this.databaseIdProvider != null)
			factory.setDatabaseIdProvider(this.databaseIdProvider);
		if (StringUtils.hasLength(this.properties.getTypeAliasesPackage()))
			factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
		if (StringUtils.hasLength(this.properties.getTypeHandlersPackage()))
			factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
		if (!ObjectUtils.isEmpty((Object[]) this.properties.resolveMapperLocations()))
			factory.setMapperLocations(this.properties.resolveMapperLocations());
		return factory.getObject();
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		ExecutorType executorType = this.properties.getExecutorType();
		if (executorType != null)
			return new SqlSessionTemplate(sqlSessionFactory, executorType);
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	public static class AutoConfiguredMapperScannerRegistrar
			implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {
		private BeanFactory beanFactory;

		private ResourceLoader resourceLoader;

		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {
			MybatisAutoConfiguration.logger.debug("Searching for mappers annotated with @Mapper");
			ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
			try {
				if (this.resourceLoader != null)
					scanner.setResourceLoader(this.resourceLoader);
				List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
				if (MybatisAutoConfiguration.logger.isDebugEnabled())
					for (String pkg : packages)
						MybatisAutoConfiguration.logger.debug("Using auto-configuration base package '{}'", pkg);
				scanner.setAnnotationClass(Mapper.class);
				scanner.registerFilters();
				scanner.doScan(StringUtils.toStringArray(packages));
			} catch (IllegalStateException ex) {
				MybatisAutoConfiguration.logger.debug(
						"Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
			}
		}

		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}

	@Configuration
	@Import({ AutoConfiguredMapperScannerRegistrar.class })
	@ConditionalOnMissingBean({ MapperFactoryBean.class })
	public static class MapperScannerRegistrarNotFoundConfiguration {
		@PostConstruct
		public void afterPropertiesSet() {
			MybatisAutoConfiguration.logger.debug("No {} found.", MapperFactoryBean.class.getName());
		}
	}
}
