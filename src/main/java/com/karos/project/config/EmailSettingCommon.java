/**
 * Title
 *
 * @ClassName: EmailSettingConfig
 * @Description:
 * @author: Karos
 * @date: 2022/12/14 21:44
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.config;

/**
 * Title
 *
 * @ClassName: MailCommon
 * @Description: 读取yml-mail信息
 * @author: Karos
 * @date: 2022/10/14 1:00
 * @Blog: https://www.wzl1.top/
 */

import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

//所有变量不能设置为静态变量
@Configuration
@ToString
public class EmailSettingCommon {
    @Value("${spring.mail.username}")
    public  String username;
    @Value("${spring.mail.host}")
    public  String host;
    @Value("${spring.mail.password}")
    public  String password;
    @Value("${spring.mail.port}")
    public  int port;
    @Value("${spring.mail.default-encoding}")
    public  String default_encoding;

    @Bean
    public static SimpleMailMessage smm(){
        return new SimpleMailMessage();
    }
}