/**
 * Title
 *
 * @ClassName: CheckCode
 * @Description:
 * @author: Karos
 * @date: 2022/12/16 17:59
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.controller.Untils;

import cn.hutool.Hutool;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.mail.MailUtil;
import com.karos.KaTool.iputils.IpUtils;
import com.karos.project.annotation.AllLimitCheck;
import com.karos.project.common.*;
import com.karos.project.exception.BusinessException;
import com.karos.project.model.dto.checkcode.CheckCodeRequest;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import com.karos.KaTool.CheckCode.GenerateCode;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/checkcode")
public class CheckCodeController {
    public static String salt="Karos20190405";
    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    EmailUtils emailUtils;
    @Autowired
    InitRedis init = new InitRedis();
    /**
     *
     * @param datestamp         时间戳
     * @param httpServletRequest
     * @param httpServletResponse
     */
    @GetMapping("/touch/{datestamp}")
    public BaseResponse touch(@PathVariable("datestamp") String datestamp, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        if (!StringUtils.isNumeric(datestamp))  throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String ip = IpUtils.getIpAddr(httpServletRequest);
        String key= DigestUtil.md5Hex(datestamp+ip);
        String code=null;
        try {
           code = GenerateCode.outputVerifyImage(200, 100, httpServletResponse.getOutputStream(), 4);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码生成失败");
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (hashOperations.hasKey("checkcode_img",key)) {
            hashOperations.delete("checkcode_img",key);
        }
        init.init();
        hashOperations.put("checkcode_img",key,code.toUpperCase(Locale.ROOT));
        return ResultUtils.success("请求成功");
    }

    @PostMapping("/send")
    @AllLimitCheck(mustText = "验证码发送")
    public BaseResponse<String> send(@RequestBody CheckCodeRequest checkCodeRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        String mail=checkCodeRequest.getUserMail();
        String code=checkCodeRequest.getCheckCode().toUpperCase(Locale.ROOT);
        String datestamp=checkCodeRequest.getDateStamp();

        if (!Validator.isEmail(mail)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        HashOperations hashOperations = redisTemplate.opsForHash();
        String key = DigestUtil.md5Hex(datestamp + IpUtils.getIpAddr(httpServletRequest));
        if ((!hashOperations.hasKey("checkcode_img",key))||code==null|| StringUtils.isAnyBlank(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入图形验证码");
        }
        String checkcode = (String) hashOperations.get("checkcode_img", key);
        if (!code.equals(checkcode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图形验证码匹配失败，请重新填写");
        }
        hashOperations.delete("checkcode_img",key);
        hashOperations.delete("checkcode_sms",mail);
        String code_sms = new GenerateCode(salt).touchTextCode(mail, 6);
        hashOperations.put("checkcode_sms",mail,code_sms.toUpperCase(Locale.ROOT));
        emailUtils.setMessage(mail,"【掌印日记】信息验证服务","【掌印日记】您的验证码为"+code_sms);
        emailUtils.send();
        return ResultUtils.success("验证码已经发送到您的邮箱");
    }
}
