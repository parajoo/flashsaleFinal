package cn.wolfcode.cache;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

@Configuration
public class AppConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService(){
        // core thread num = core*2+2
        return new ScheduledThreadPoolExecutor(10);
    }

}
