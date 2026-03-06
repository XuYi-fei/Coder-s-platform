package com.github.paicoding.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Configuration
@ComponentScan("com.github.paicoding.forum.service")
@MapperScan(basePackages = {
        "com.github.paicoding.forum.service.user.repository.mapper",
        "com.github.paicoding.forum.service.chatv2.repository.mapper",
        "com.github.paicoding.forum.service.agent.repository.mapper",
        "com.github.paicoding.forum.service.image.oss",})
public class ServiceAutoConfig {

}
