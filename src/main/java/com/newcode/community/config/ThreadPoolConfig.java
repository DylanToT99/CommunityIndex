package com.newcode.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/1 18:47
 * @description TODO
 **/
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
