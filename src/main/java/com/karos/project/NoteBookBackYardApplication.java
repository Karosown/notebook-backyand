package com.karos.project;

import cn.hutool.core.codec.Base64;
import com.karos.project.common.InitRedis;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.karos.project.mapper")
public class NoteBookBackYardApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteBookBackYardApplication.class, args);
    }

}
