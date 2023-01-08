/**
 * Title
 *
 * @ClassName: FileUploadIntercepotor
 * @Description:
 * @author: Karos
 * @date: 2022/12/15 10:35
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.aop;

import com.karos.KaTool.iputils.IpUtils;
import com.karos.project.common.ErrorCode;
import com.karos.project.common.FileLimit;
import com.karos.project.common.IPFileDownloadNum;
import com.karos.project.common.ResultUtils;
import com.karos.project.config.FinalvarConfig;
import com.karos.project.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.karos.project.annotation.AllLimitCheck;
@Aspect
@Component
@Slf4j
public class FileUploadIntercepotor {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    FinalvarConfig fcf;


    @Around("@annotation(AllLimitCheck)")
    public Object doInterceptor(ProceedingJoinPoint point, AllLimitCheck AllLimitCheck) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        //判断IP上传限制
            //获取ip地址
        String ip = IpUtils.getIpAddr(httpServletRequest);
        HashOperations hashOperations = redisTemplate.opsForHash();
        FileLimit fileLimit=null;
        //判断当前类型是否有
        if (!hashOperations.hasKey("FileLimit",AllLimitCheck.mustText())) {
            ConcurrentHashMap<String, IPFileDownloadNum> ipFileDownloadNums = new ConcurrentHashMap<>();
            ipFileDownloadNums.put(ip,new IPFileDownloadNum(1L,new Date(new Date().getTime()+fcf.MAX_FERQUENCY)));
            fileLimit=new FileLimit(AllLimitCheck.mustText(), ipFileDownloadNums);
           hashOperations.put("FileLimit", AllLimitCheck.mustText(),fileLimit);
        }
       else{
            fileLimit = (FileLimit) hashOperations.get("FileLimit", AllLimitCheck.mustText());
            ConcurrentHashMap<String, IPFileDownloadNum> ipFileDownloadNums = fileLimit.getIpFileDownloadNums();
                IPFileDownloadNum ipFileDownloadNum = ipFileDownloadNums.get(ip);
                if (ipFileDownloadNum!=null){
                    if (ipFileDownloadNum.getExpTime().getTime()>=new Date().getTime()){
                        hashOperations.delete("FileLimit", AllLimitCheck.mustText());
                        ipFileDownloadNum.setFreQuency(0L);
                        ipFileDownloadNum.setExpTime(new Date(new Date().getTime()+ fcf.EXP_TIME));
                        ipFileDownloadNums.remove(ip);
                        ipFileDownloadNums.put(ip,ipFileDownloadNum.inc());
                        hashOperations.put("FileLimit", AllLimitCheck.mustText(), fileLimit);
                    }else {
                        if (ipFileDownloadNum.getFreQuency() >= fcf.MAX_FERQUENCY)
                            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, AllLimitCheck.mustText()+"次数限制");
                        hashOperations.delete("FileLimit",AllLimitCheck.mustText());
                        ipFileDownloadNums.remove(ip);
                        ipFileDownloadNums.put(ip,ipFileDownloadNum.inc());
                        hashOperations.put(
                                "FileLimit", AllLimitCheck.mustText(),
                                fileLimit
                        );
                    }
//                    log.info("{}",ipFileDownloadNums==fileLimit.getIpFileDownloadNums());
                }
        }
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString();
        String url = httpServletRequest.getRequestURI();
        // 获取请求参数
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        // 输出请求日志
        log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url, ip, reqParam);
        // 执行原方法
        Object result = point.proceed();
        // 输出响应日志
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
        return result;
    }
}
