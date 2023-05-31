package com.pttl.mybatis.datasource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * 
 * @ClassName: TransactionManagementConfigurerBean
 * @Description: 事务管理器
 * @author jackson.song
 * @mail suxuan696@gmail.com
 * @date 2021年7月13日
 *
 */
@Component
public class TransactionManagementConfigurerBean implements TransactionManagementConfigurer {

	@Override
	public TransactionManager annotationDrivenTransactionManager() {
		DataSourceTransactionManager dtm = new DataSourceTransactionManager();
		dtm.setDataSource(DynamicRoutingDataSource.getInstance());
		return dtm;
	}
}