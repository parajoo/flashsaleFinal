package cn.wolfcode.config;

import cn.wolfcode.job.SeckillProductInitJob;
import cn.wolfcode.job.UserCacheJob;
import cn.wolfcode.util.ElasticJobUtil;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessJobConfig {
    @Bean(initMethod = "init")
    public SpringJobScheduler initUserCacheJob(CoordinatorRegistryCenter registryCenter, UserCacheJob userCacheJob) {
        LiteJobConfiguration jobConfiguration = ElasticJobUtil.createDefaultSimpleJobConfiguration(userCacheJob.getClass(), userCacheJob.getCron());
        SpringJobScheduler springJobScheduler = new SpringJobScheduler(userCacheJob, registryCenter, jobConfiguration);
        return springJobScheduler;
    }

    @Bean(initMethod = "init")
    public SpringJobScheduler initSeckillProductListJob(CoordinatorRegistryCenter registryCenter, SeckillProductInitJob job) {
        LiteJobConfiguration jobConfiguration =
                ElasticJobUtil.createJobConfiguration(
                        job.getClass(),
                        job.getCron(),
                        job.getShardingCount(),
                        job.getShardingParameters(),
                        job.isDataFlow());
        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
    }
}
