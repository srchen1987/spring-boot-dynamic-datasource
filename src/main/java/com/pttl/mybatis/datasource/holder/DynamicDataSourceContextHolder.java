package com.pttl.mybatis.datasource.holder;


/**
 * 
* @ClassName: DynamicDataSourceContextHolder 
* @Description: TLS 存储使用数据源标识
* @author jackson.song
* @mail suxuan696@gmail.com
* @date 2020年6月3日
*
 */
public class DynamicDataSourceContextHolder {
  private static final ThreadLocal<String> current = new ThreadLocal<>();
  
  public static void clear() {
    current.remove();
  }
  
  public static String get() {
    return current.get();
  }
  
  public static void set(String value) {
    current.set(value);
  }
}
