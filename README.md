基于Spring实现的动态数据源框架
使用方法如下：

## 配置文件
 在springboot的配置文件中配置多个数据源
 其中prefix = "spring.slavedatasource" 的配置文件为从库，datasource为主库
 ```
 spring.datasource.url=jdbc:mysql://127.0.0.1:3306/user?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.slavedatasource.url=jdbc:mysql://127.0.0.1:3306/user_slave?useUnicode=true&characterEncoding=utf-8
spring.slavedatasource.username=root
spring.slavedatasource.password=
spring.slavedatasource.driver-class-name=com.mysql.cj.jdbc.Driver
 


 ```
## 定义bean
 
 下面是一个定义bean的demo  
```
@Component
public class SlaveDsConfig {
    @Bean(name="slaveDataSource")
    @ConfigurationProperties(prefix = "spring.slavedatasource")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean(name="defaultDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```
## 指定数据源

在mapper的接口层使用注解 @TargetDataSource

``` 
public interface UserMapper {

 	@TargetDataSource("slaveDataSource")
    public User findUserInfo();
    
// 	@TargetDataSource("defaultDataSource") 主库可以不配置，默认为空的情况会寻找defaultDataSource
    public User insertUser(User user);
}

```
**注意事项：**
@ComponentScan({"其他包","com.pttl.mybatis.datasource"}) 这个注解是需要的 如果没有不会生效
