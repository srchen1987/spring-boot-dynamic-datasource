package com.pttl.mybatis.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
* @ClassName: TargetDataSource 
* @Description: 目标数据源 value则为数据源名称
* @author jackson.song
* @mail suxuan696@gmail.com
* @date 2020年6月3日
*
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetDataSource {
  String value() default "";
}
