package com.pttl.mybatis.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.pttl.mybatis.datasource.holder.DynamicDataSourceContextHolder;

/**
 * 
 * @ClassName: DynamicRoutingDataSource
 * @Description: 路由动态数据源的一个实现
 * @author jackson.song
 * @mail suxuan696@gmail.com
 * @date 2020年6月3日
 *
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
	private static final DynamicRoutingDataSource instance = new DynamicRoutingDataSource();

	protected Object determineCurrentLookupKey() {
		return DynamicDataSourceContextHolder.get();
	}

	public static DynamicRoutingDataSource getInstance() {
		return instance;
	}
}
