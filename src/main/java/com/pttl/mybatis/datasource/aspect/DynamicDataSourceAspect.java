package com.pttl.mybatis.datasource.aspect;

import com.pttl.mybatis.datasource.annotation.TargetDataSource;
import com.pttl.mybatis.datasource.holder.DynamicDataSourceContextHolder;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
* @ClassName: DynamicDataSourceAspect 
* @Description: 基于spring aop 做的切面类 来选择数据源
* @author jackson.song
* @mail suxuan696@gmail.com
* @date 2020年6月3日
*
 */
@Aspect
@Order(1)
@Component
public class DynamicDataSourceAspect {
  @Before("@annotation(com.pttl.mybatis.datasource.annotation.TargetDataSource)")
  public void doBefore(JoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
    Method method = methodSignature.getMethod();
    if (method.getDeclaringClass().isInterface())
      try {
        method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(), method.getParameterTypes());
      } catch (NoSuchMethodException noSuchMethodException) {} 
    TargetDataSource targetDataSource = method.<TargetDataSource>getAnnotation(TargetDataSource.class);
    if (null != targetDataSource) {
      String dataSourceKey = targetDataSource.value();
      DynamicDataSourceContextHolder.set(dataSourceKey);
    } 
  }
  
  @After("@annotation(com.pttl.mybatis.datasource.annotation.TargetDataSource)")
  public void doAfter(JoinPoint joinPoint) {
    DynamicDataSourceContextHolder.clear();
  }
}
