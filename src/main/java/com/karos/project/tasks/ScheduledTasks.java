/**
 * Title
 *
 * @ClassName: ScheduledTasks
 * @Description:
 * @author: Karos
 * @date: 2023/1/5 3:32
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.tasks;

import cn.katool.lock.LockUtil;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableAsync
@EnableScheduling
public abstract class ScheduledTasks {
    @Resource
    protected LockUtil lockUtil;

}
