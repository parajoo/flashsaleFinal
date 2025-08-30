package cn.wolfcode.config;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册中心配置对象
 */
@Configuration
public class RegistryCenterConfig {
    @Bean(initMethod = "init")
    public CoordinatorRegistryCenter createRegistryCenter(@Value("${elasticjob.zookeeper-url}") String zookeeperUrl, @Value("${elasticjob.group-name}") String groupName) {
        //zk的配置
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(zookeeperUrl,groupName);
        //设置zk超时时间
        zookeeperConfiguration.setSessionTimeoutMilliseconds(100);
        //创建注册中心
        CoordinatorRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        return zookeeperRegistryCenter;
    }
}