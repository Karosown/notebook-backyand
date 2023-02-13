/**
 * Title
 *
 * @ClassName: TasksTest
 * @Description:
 * @author: Karos
 * @date: 2023/1/6 3:07
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.tasks;

import cn.hutool.core.lang.func.VoidFunc0;
import cn.katool.Exception.KaToolException;
import cn.katool.lock.LockUtil;
import cn.katool.other.MethodIntefaceUtil;
import com.karos.project.constant.LockConstant;
import com.karos.project.constant.RedisKeysConstant;
import com.karos.project.model.entity.Note;
import com.karos.project.model.entity.Notethumbrecords;
import com.karos.project.service.NoteService;
import com.karos.project.service.impl.NotethumbrecordsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.karos.project.constant.RedisKeysConstant.ThumbsHistoryHash;
import static com.karos.project.constant.RedisKeysConstant.ThumbsUserSet;

@SpringBootTest
public class NoteScheduledTasksTest {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private NoteService noteService;
    @Resource
    private LockUtil lockUtil;
    @Resource
    private NotethumbrecordsServiceImpl notethumbrecordsService;

    @Test
    void ThumbPersTest() throws KaToolException {
        //加锁
        lockUtil.DistributedLock(LockConstant.ThumbsLock_Pers,10L, TimeUnit.SECONDS);
        //持久化
        //list 用于获取点赞的用户
        SetOperations setOperations = redisTemplate.opsForSet();
        //hash 用于获取用户点赞数据
        HashOperations hashOperations = redisTemplate.opsForHash();
        //从缓存中取出点赞过的用户ID
        Long usersetsize = setOperations.size(ThumbsUserSet);
        //如果没有人点赞，那就释放锁，并且退出
        if (usersetsize<=0L){
            lockUtil.DistributedUnLock(LockConstant.ThumbsLock_Pers);
            return;
        }
        List<String> userlist =(List<String>)setOperations.pop(ThumbsUserSet,usersetsize);
        //清楚点过赞的用户
        redisTemplate.delete(ThumbsUserSet);
        ArrayList<CompletableFuture<Void> > futrueList=new ArrayList<>();
        //获取所有用户点赞过的列表
        List<Notethumbrecords> thumblist = hashOperations.multiGet(ThumbsHistoryHash, userlist);
        Map entries = hashOperations.entries(RedisKeysConstant.ThumbsNum);
        int i=0;
        int j=0;
        Set set = entries.keySet();
        Iterator iterator = set.iterator();
        int size=set.size();
        while(true){
            if (j>=thumblist.size())break;
            ArrayList<Notethumbrecords> historyList=new ArrayList<>();
            ArrayList<Note> countList=new ArrayList<>();
            while(j++%1000!=0) {
                Notethumbrecords e = thumblist.get(j);
                if (e==null) break;
                historyList.add(e);
            }
            while(i++%1000!=0&&iterator.hasNext()){
                String noteID = (String) iterator.next();
                Long thumbNum= (Long) entries.get(noteID);
                Note temp=new Note();
                temp.setId(noteID);
                temp.setThumbNum(thumbNum);
                countList.add(temp);
            }
            //开启多线程
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //将点赞数据持久化到mysql
                notethumbrecordsService.saveOrUpdateBatch(historyList, 10000);
                noteService.updateBatchById(countList);
            });
            futrueList.add(future);
        }
        CompletableFuture.allOf(futrueList.toArray(new CompletableFuture[]{})).join();
//                notethumbrecordsService.saveOrUpdateBatch(thumblist,10000);
        //释放锁
        lockUtil.DistributedUnLock(LockConstant.ThumbsLock_Pers);    }
}
