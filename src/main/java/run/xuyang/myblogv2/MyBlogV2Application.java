package run.xuyang.myblogv2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@MapperScan("run.xuyang.myblogv2.mapper")
@EnableScheduling
public class MyBlogV2Application {

    public static void main(String[] args) {
        SpringApplication.run(MyBlogV2Application.class, args);
    }

    /**
     * springboot中，默认的定时任务线程池是只有一个线程的,
     * 配置这个用于解决多个定时任务时可能出现错误
     *
     * @return taskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("springboot task");
        return taskScheduler;
    }
}
