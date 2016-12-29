/**
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import psiprobe.beans.ClusterWrapperBean;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.beans.ContainerWrapperBean;
import psiprobe.beans.JBossResourceResolverBean;
import psiprobe.beans.JvmMemoryInfoAccessorBean;
import psiprobe.beans.LogResolverBean;
import psiprobe.beans.ResourceResolver;
import psiprobe.beans.ResourceResolverBean;
import psiprobe.beans.RuntimeInfoAccessorBean;
import psiprobe.beans.stats.collectors.AppStatsCollectorBean;
import psiprobe.beans.stats.collectors.ClusterStatsCollectorBean;
import psiprobe.beans.stats.collectors.ConnectorStatsCollectorBean;
import psiprobe.beans.stats.collectors.DatasourceStatsCollectorBean;
import psiprobe.beans.stats.collectors.JvmMemoryStatsCollectorBean;
import psiprobe.beans.stats.collectors.RuntimeStatsCollectorBean;
import psiprobe.beans.stats.listeners.MemoryPoolMailingListener;
import psiprobe.beans.stats.listeners.StatsCollectionListener;
import psiprobe.beans.stats.providers.ConnectorSeriesProvider;
import psiprobe.beans.stats.providers.MultipleSeriesProvider;
import psiprobe.beans.stats.providers.StandardSeriesProvider;
import psiprobe.model.stats.StatsCollection;
import psiprobe.tools.Mailer;

/**
 * The Class ProbeConfig.
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = {"psiprobe"})
public class ProbeConfig extends WebMvcConfigurerAdapter {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ProbeConfig.class);

  /**
   * Gets the container listener bean.
   *
   * @return the container listener bean
   */
  @Bean(name = "containerListener")
  public ContainerListenerBean getContainerListenerBean() {
    logger.info("Instantiated containerListener");
    return new ContainerListenerBean();
  }

  /**
   * Gets the container wrapper bean.
   *
   * @return the container wrapper bean
   */
  @Bean(name = "containerWrapper")
  public ContainerWrapperBean getContainerWrapperBean() {
    logger.info("Instantiated containerWrapper");
    return new ContainerWrapperBean();
  }

  /**
   * Gets the cluster wrapper bean.
   *
   * @return the cluster wrapper bean
   */
  @Bean(name = "clusterWrapper")
  public ClusterWrapperBean getClusterWrapperBean() {
    logger.info("Instantiated clusterWrapper");
    return new ClusterWrapperBean();
  }

  /**
   * Gets the mailer.
   *
   * @return the mailer
   */
  @Bean(name = "mailer")
  public Mailer getMailer() {
    logger.info("Instantiated mailer");
    return new Mailer();
  }

  /**
   * Gets the default res.
   *
   * @return the default res
   */
  @Bean(name = "datasourceMappers")
  public List<String> getDefaultRes() {
    logger.info("Instantiated datasourceMappers");
    List<String> list = new ArrayList<>();
    list.add("psiprobe.beans.BoneCpDatasourceAccessor");
    list.add("psiprobe.beans.C3P0DatasourceAccessor");
    list.add("psiprobe.beans.DbcpDatasourceAccessor");
    list.add("psiprobe.beans.Dbcp2DatasourceAccessor");
    list.add("psiprobe.beans.Tomcat7DbcpDatasourceAccessor");
    list.add("psiprobe.beans.Tomcat8DbcpDatasourceAccessor");
    list.add("psiprobe.beans.Tomcat85DbcpDatasourceAccessor");
    list.add("psiprobe.beans.Tomcat9DbcpDatasourceAccessor");
    list.add("psiprobe.beans.TomcatJdbcPoolDatasourceAccessor");
    list.add("psiprobe.beans.OracleDatasourceAccessor");
    list.add("psiprobe.beans.OracleUcpDatasourceAssessor");
    list.add("psiprobe.beans.OpenEjbManagedDatasourceAccessor");
    return list;
  }

  /**
   * Gets the resource resolver bean.
   *
   * @return the resource resolver bean
   */
  @Bean(name = "default")
  public ResourceResolverBean getResourceResolverBean() {
    logger.info("Instantiated default resourceResolverBean");
    return new ResourceResolverBean();
  }

  /**
   * Gets the jboss resource resolver bean.
   *
   * @return the jboss resource resolver bean
   */
  @Bean(name = "jboss")
  public JBossResourceResolverBean getJBossResourceResolverBean() {
    logger.info("Instantiated jbossResourceResolverBean");
    return new JBossResourceResolverBean();
  }

  /**
   * Gets the resource resolvers.
   *
   * @param jbossResourceResolverBean the jboss resource resolver bean
   * @param resourceResolverBean the resource resolver bean
   * @return the resource resolvers
   */
  @Bean(name = "resourceResolvers")
  public Map<String, ResourceResolver> getResourceResolvers(
      @Autowired JBossResourceResolverBean jbossResourceResolverBean,
      @Autowired ResourceResolverBean resourceResolverBean) {
    logger.info("Instantiated resourceResolvers");
    Map<String, ResourceResolver> map = new HashMap<>();
    map.put("jboss", jbossResourceResolverBean);
    map.put("default", resourceResolverBean);
    return map;
  }

  /**
   * Gets the adapter classes.
   *
   * @return the adapter classes
   */
  // TODO We should make this configurable
  @Bean(name = "adapterClasses")
  public List<String> getAdapterClasses() {
    logger.info("Instantiated adapterClasses");
    List<String> list = new ArrayList<>();
    list.add("psiprobe.Tomcat90ContainerAdapter");
    list.add("psiprobe.Tomcat85ContainerAdapter");
    list.add("psiprobe.Tomcat80ContainerAdapter");
    list.add("psiprobe.Tomcat70ContainerAdapter");
    return list;
  }

  /**
   * Gets the stdout files. Any file added to this list will be displayed.
   *
   * @return the stdout files
   */
  // TODO We should make this configurable
  @Bean(name = "stdoutFiles")
  public List<String> getStdoutFiles() {
    logger.info("Instantiated stdoutFiles");
    List<String> list = new ArrayList<>();
    list.add("catalina.out");
    list.add("wrapper.log");
    list.add("stdout.log");
    list.add("stdout.err");
    return list;
  }

  /**
   * Gets the log resolver bean.
   *
   * @return the log resolver bean
   */
  @Bean(name = "logResolver")
  public LogResolverBean getLogResolverBean() {
    logger.info("Instantiated logResolver");
    return new LogResolverBean();
  }

  /**
   * Gets the stats collection.
   *
   * @return the stats collection
   */
  @Bean(name = "statsCollection")
  public StatsCollection getStatsCollection() {
    logger.info("Instantiated statsCollection");
    return new StatsCollection();
  }

  /**
   * Gets the connector stats collector bean.
   *
   * @return the connector stats collector bean
   */
  @Bean(name = "connectorStatsCollector")
  public ConnectorStatsCollectorBean getConnectorStatsCollectorBean() {
    logger.info("Instantiated connectorStatsCollector");
    return new ConnectorStatsCollectorBean();
  }

  /**
   * Gets the cluster stats collector bean.
   *
   * @return the cluster stats collector bean
   */
  @Bean(name = "clusterStatsCollector")
  public ClusterStatsCollectorBean getClusterStatsCollectorBean() {
    logger.info("Instantiated clusterStatsCollector");
    return new ClusterStatsCollectorBean();
  }

  /**
   * Gets the runtime stats collector bean.
   *
   * @return the runtime stats collector bean
   */
  @Bean(name = "runtimeStatsCollector")
  public RuntimeStatsCollectorBean getRuntimeStatsCollectorBean() {
    logger.info("Instantiated runtimeStatsCollector");
    return new RuntimeStatsCollectorBean();
  }

  /**
   * Gets the app stats collector bean.
   *
   * @return the app stats collector bean
   */
  @Bean(name = "appStatsCollector")
  public AppStatsCollectorBean getAppStatsCollectorBean() {
    logger.info("Instantiated appStatsCollector");
    return new AppStatsCollectorBean();
  }

  /**
   * Gets the jvm memory stats collector bean.
   *
   * @return the jvm memory stats collector bean
   */
  @Bean(name = "memoryStatsCollector")
  public JvmMemoryStatsCollectorBean getJvmMemoryStatsCollectorBean() {
    logger.info("Instantiated memoryStatsCollector");
    return new JvmMemoryStatsCollectorBean();
  }

  /**
   * Gets the datasource stats collector bean.
   *
   * @return the datasource stats collector bean
   */
  @Bean(name = "datasourceStatsCollector")
  public DatasourceStatsCollectorBean getDatasourceStatsCollectorBean() {
    logger.info("Instantiated datasourceStatsCollector");
    return new DatasourceStatsCollectorBean();
  }

  /**
   * Gets the jvm memory info accessor bean.
   *
   * @return the jvm memory info accessor bean
   */
  @Bean(name = "jvmMemoryInfoAccessor")
  public JvmMemoryInfoAccessorBean getJvmMemoryInfoAccessorBean() {
    logger.info("Instantiated jvmMemoryInfoAccessorBean");
    return new JvmMemoryInfoAccessorBean();
  }

  /**
   * Gets the runtime info accessor bean.
   *
   * @return the runtime info accessor bean
   */
  @Bean(name = "runtimeInfoAccessor")
  public RuntimeInfoAccessorBean getRuntimeInfoAccessorBean() {
    logger.info("Instantiated runtimeInfoAccessorBean");
    return new RuntimeInfoAccessorBean();
  }

  /**
   * Gets the memory pool mailing listener.
   *
   * @return the memory pool mailing listener
   */
  @Bean(name = "listeners")
  public List<StatsCollectionListener> getMemoryPoolMailingListener() {
    logger.info("Instantiated listeners");
    List<StatsCollectionListener> list = new ArrayList<>();
    list.add(new MemoryPoolMailingListener());
    return list;
  }

  /**
   * Gets the internal resource view resolver.
   *
   * @return the internal resource view resolver
   */
  @Bean(name = "jspViewResolver")
  public ViewResolver getViewResolver() {
    logger.info("Instantiated internalResourceViewResolver");
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setViewClass(JstlView.class);
    resolver.setPrefix("/WEB-INF/jsp/");
    resolver.setSuffix(".jsp");
    return resolver;
  }

  /**
   * Gets the fixed theme resolver.
   *
   * @return the fixed theme resolver
   */
  @Bean(name = "themeResolver")
  public ThemeResolver getThemeResolver() {
    logger.info("Instantiated fixedThemeResolver");
    FixedThemeResolver resolver = new FixedThemeResolver();
    resolver.setDefaultThemeName("theme-classic");
    return resolver;
  }

  /**
   * Gets the reloadable resource bundle message source.
   *
   * @return the reloadable resource bundle message source
   */
  @Bean(name = "messageSource")
  public MessageSource getMessageSource() {
    logger.info("Instantiated reloadableResourceBundleMessageSource");
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("/WEB-INF/messages");
    source.setCacheSeconds(1);
    source.setFallbackToSystemLocale(false);
    return source;
  }

  /**
   * Gets the cookie locale resolver.
   *
   * @return the cookie locale resolver
   */
  @Bean(name = "localeResolver")
  public LocaleResolver getLocaleResolver() {
    logger.info("Instantiated cookieLocaleResolver");
    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
  }

  /**
   * Gets the bean name url handler mapping.
   *
   * @return the bean name url handler mapping
   */
  @Bean(name = "handlerMapping")
  public HandlerMapping getHandlerMapping(@Autowired LocaleChangeInterceptor interceptor) {
    logger.info("Instantiated beanNameUrlHandlerMapping");
    BeanNameUrlHandlerMapping mapping = new BeanNameUrlHandlerMapping();
    mapping.setAlwaysUseFullPath(true);
    mapping.setInterceptors(new Object[] {interceptor});
    return mapping;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    logger.info("Registering localeChangeInterceptor");
    registry.addInterceptor(getLocaleChangeInterceptor());
  }

  /**
   * Gets the locale change interceptor.
   *
   * @return the locale change interceptor
   */
  @Bean(name = "localeChangeInterceptor")
  public LocaleChangeInterceptor getLocaleChangeInterceptor() {
    logger.info("Instantiated localeChangeInterceptor");
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    interceptor.setParamName("lang");
    return interceptor;
  }

  /**
   * Gets the property placeholder configurer.
   *
   * @return the property placeholder configurer
   */
  @Bean(name = "propertyPlaceholderConfigurer")
  public static PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
    logger.info("Instantiated propertyPlaceholderConfigurer");
    PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
    configurer.setLocation(new ClassPathResource("stats.properties"));
    configurer.setNullValue("NULL");

    Properties properties = new Properties();
    properties.put("psiprobe.tools.mail.to", "NULL");
    properties.put("psiprobe.tools.mail.subjectPrefix", "[PSI Probe]");
    configurer.setProperties(properties);

    configurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");

    return configurer;
  }

  /**
   * Version.
   *
   * @return the properties factory bean
   */
  @Bean(name = "version")
  public PropertiesFactoryBean version() {
    logger.info("Instantiated version");
    PropertiesFactoryBean bean = new PropertiesFactoryBean();
    bean.setLocation(new ClassPathResource("version.properties"));
    return bean;
  }

  /**
   * Gets the connector series provider.
   *
   * @return the connector series provider
   */
  @Bean(name = "rcn")
  public ConnectorSeriesProvider getConnectorSeriesProvider() {
    logger.info("Instantiated rcn");
    return new ConnectorSeriesProvider();
  }

  /**
   * Gets the cl traffic.
   *
   * @return the cl traffic
   */
  @Bean(name = "cl_traffic")
  public StandardSeriesProvider getClTraffic() {
    logger.info("Instantiated cl_traffic");
    List<String> list = new ArrayList<>();
    list.add("cluster.sent");
    list.add("cluster.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the cl request.
   *
   * @return the cl request
   */
  @Bean(name = "cl_request")
  public StandardSeriesProvider getClRequest() {
    logger.info("Instantiated cl_request");
    List<String> list = new ArrayList<>();
    list.add("cluster.req.sent");
    list.add("cluster.req.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the connector.
   *
   * @return the connector
   */
  @Bean(name = "connector")
  public StandardSeriesProvider getConnector() {
    logger.info("Instantiated connector");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.requests");
    list.add("stat.connector.{0}.errors");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the traffic.
   *
   * @return the traffic
   */
  @Bean(name = "traffic")
  public StandardSeriesProvider getTraffic() {
    logger.info("Instantiated traffic");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.sent");
    list.add("stat.connector.{0}.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the connector proc time.
   *
   * @return the connector proc time
   */
  @Bean(name = "connector_proc_time")
  public StandardSeriesProvider getConnectorProcTime() {
    logger.info("Instantiated connector_proc_time");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.proc_time");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the memory usage.
   *
   * @return the memory usage
   */
  @Bean(name = "memory_usage")
  public StandardSeriesProvider getMemoryUsage() {
    logger.info("Instantiated memory_usage");
    List<String> list = new ArrayList<>();
    list.add("memory.pool.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the os memory.
   *
   * @return the os memory
   */
  @Bean(name = "os_memory")
  public StandardSeriesProvider getOsMemory() {
    logger.info("Instantiated os_memory");
    List<String> list = new ArrayList<>();
    list.add("os.memory.physical");
    list.add("os.memory.committed");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the swap usage.
   *
   * @return the swap usage
   */
  @Bean(name = "swap_usage")
  public StandardSeriesProvider getSwapUsage() {
    logger.info("Instantiated swap_usage");
    List<String> list = new ArrayList<>();
    list.add("os.memory.swap");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the cpu usage.
   *
   * @return the cpu usage
   */
  @Bean(name = "cpu_usage")
  public StandardSeriesProvider getCpuUsage() {
    logger.info("Instantiated cpu_usage");
    List<String> list = new ArrayList<>();
    list.add("os.cpu");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the fd usage.
   *
   * @return the fd usage
   */
  @Bean(name = "fd_usage")
  public StandardSeriesProvider getFdUsage() {
    logger.info("Instantiated fd_usage");
    List<String> list = new ArrayList<>();
    list.add("os.fd.open");
    list.add("os.fd.max");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the app req.
   *
   * @return the app req
   */
  @Bean(name = "app_req")
  public StandardSeriesProvider getAppReq() {
    logger.info("Instantiated app_req");
    List<String> list = new ArrayList<>();
    list.add("app.requests.{0}");
    list.add("app.errors.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the app avg proc time.
   *
   * @return the app avg proc time
   */
  @Bean(name = "app_avg_proc_time")
  public StandardSeriesProvider getAppAvgProcTime() {
    logger.info("Instantiated app_avg_proc_time");
    List<String> list = new ArrayList<>();
    list.add("app.avg_proc_time.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the total avg proc time.
   *
   * @return the total avg proc time
   */
  @Bean(name = "total_avg_proc_time")
  public StandardSeriesProvider getTotalAvgProcTime() {
    logger.info("Instantiated total_avg_proc_time");
    List<String> list = new ArrayList<>();
    list.add("total.avg_proc_time");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the total req.
   *
   * @return the total req
   */
  @Bean(name = "total_req")
  public StandardSeriesProvider getTotalReq() {
    logger.info("Instantiated total_req");
    List<String> list = new ArrayList<>();
    list.add("total.requests");
    list.add("total.errors");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the datasource usage.
   *
   * @return the datasource usage
   */
  @Bean(name = "datasource_usage")
  public StandardSeriesProvider getDatasourceUsage() {
    logger.info("Instantiated datasource_usage");
    List<String> list = new ArrayList<>();
    list.add("ds.est.{0}");
    list.add("ds.busy.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the all app avg proc time.
   *
   * @return the all app avg proc time
   */
  @Bean(name = "all_app_avg_proc_time")
  public MultipleSeriesProvider getAllAppAvgProcTime() {
    logger.info("Instantiated all_app_avg_proc_time");
    MultipleSeriesProvider provider = new MultipleSeriesProvider();
    provider.setMovingAvgFrame(10);
    provider.setStatNamePrefix("app.avg_proc_time.");
    provider.setTop(4);
    return provider;
  }

  /**
   * Gets the all app req.
   *
   * @return the all app req
   */
  @Bean(name = "all_app_req")
  public MultipleSeriesProvider getAllAppReq() {
    logger.info("Instantiated all_app_req");
    MultipleSeriesProvider provider = new MultipleSeriesProvider();
    provider.setMovingAvgFrame(10);
    provider.setStatNamePrefix("app.requests.");
    provider.setTop(4);
    return provider;
  }

}