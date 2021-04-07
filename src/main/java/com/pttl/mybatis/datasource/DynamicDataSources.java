package com.pttl.mybatis.datasource;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
/**
 * 
* @ClassName: DynamicDataSources 
* @Description: 动态数据源
* @author jackson.song
* @mail suxuan696@gmail.com
* @date 2020年6月3日
*
 */
@Component
public class DynamicDataSources implements ApplicationContextAware, InitializingBean {
  private ApplicationContext applicationContext;
  
  private Map<Object, Object> dataSources = new HashMap<>();
  
  public Map<Object, Object> getDataSource() {
    return this.dataSources;
  }
  
  public void afterPropertiesSet() throws Exception {
    String[] names = this.applicationContext.getBeanNamesForType(DataSource.class);
    for (String name : names) {
      Object obj = this.applicationContext.getBean(name);
      this.dataSources.put(name, obj);
    } 
  }
  
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
