/**
 * Title
 *
 * @ClassName: EmailUtils
 * @Description:
 * @author: 巫宗霖
 * @date: 2022/12/14 21:48
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

/**
 * Title
 *
 * @ClassName: EmailSend
 * @Description:
 * @author: Karos
 * @date: 2022/10/15 2:55
 * @Blog: https://www.wzl1.top/
 */


import com.karos.project.config.EmailSettingCommon;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Log4j2
public class EmailUtils {
    @Autowired
    private JavaMailSenderImpl impl;

    @Autowired
    private SimpleMailMessage smm;

    @Autowired
    private EmailSettingCommon esc;

    public void setMessage(String toMail,String Subject,String Text){
//       if (impl.getPassword()==null)SmmInit(); //如果无法读取到yml配置文件可以试一试这个，Spring自动装配@Autowired不建议使用与静态对象，@Value只有在自动装配的时候才会写入
        smm.setFrom(esc.username);
        smm.setTo(toMail);
        smm.setSubject(Subject);
        smm.setText(Text);
        smm.setSentDate(new Date());
        log.info(impl.getPassword());
    }

//    private void SmmInit(){
//        impl.setPassword(esc.password);
//        impl.setUsername(esc.username);
//        impl.setHost(esc.host);
//        impl.setDefaultEncoding(esc.default_encoding);
//        impl.setPort(esc.port);
//    }

    public void send(){
        impl.send(smm);
    }
}