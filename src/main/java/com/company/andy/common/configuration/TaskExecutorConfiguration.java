package com.company.andy.common.configuration;

import com.company.andy.common.tracing.ActorMdcPropagatingTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;

import static com.company.andy.common.utils.Constants.APPLICATION_TASK_EXECUTOR;
import static com.company.andy.common.utils.Constants.THREAD_POOL_TASK_EXECUTOR;

@Slf4j
@EnableAsync
@Configuration
public class TaskExecutorConfiguration implements AsyncConfigurer {

    @Primary
    @Bean(name = APPLICATION_TASK_EXECUTOR) // Default task executor uses virtual threads
    public TaskExecutor applicationTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        executor.setThreadNamePrefix("applicationTaskExecutor-");
        executor.setConcurrencyLimit(1000);
        executor.setTaskDecorator(taskDecorator());
        return executor;
    }

    @Bean(THREAD_POOL_TASK_EXECUTOR)
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("threadPoolTaskExecutor-");
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(false);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(taskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskDecorator taskDecorator() {
        return new CompositeTaskDecorator(List.of(
                new ContextPropagatingTaskDecorator(), // Handles propagation for Spring Security context and Micrometer tracing context
                new ActorMdcPropagatingTaskDecorator() // Handles propagation for Actor MDC context
        ));
    }

    @Override
    public Executor getAsyncExecutor() {
        return applicationTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async error happened in method {}.", method.getName(), ex);
        };
    }
}
