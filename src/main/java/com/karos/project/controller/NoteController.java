/**
 * Title
 *
 * @ClassName: NoteController
 * @Description:
 * @author: 巫宗霖
 * @date: 2022/12/28 1:58
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.controller;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.karos.KaTool.iputils.IpUtils;
import com.karos.KaTool.lock.LockUtil;
import com.karos.KaTool.qiniu.impl.QiniuServiceImpl;
import com.karos.project.annotation.AuthCheck;
import com.karos.project.common.BaseResponse;
import com.karos.project.common.DeleteRequest;
import com.karos.project.common.ErrorCode;
import com.karos.project.common.ResultUtils;
import com.karos.project.constant.CommonConstant;
import com.karos.project.constant.RedisKeysConstant;
import com.karos.project.exception.BusinessException;
import com.karos.project.model.dto.note.NoteAddRequest;
import com.karos.project.model.dto.note.NoteDoThumbRequest;
import com.karos.project.model.dto.note.NoteQueryRequest;
import com.karos.project.model.dto.note.NoteUpdateRequest;
import com.karos.project.model.entity.Note;
import com.karos.project.model.entity.Notehistory;
import com.karos.project.model.entity.Notethumbrecords;
import com.karos.project.model.entity.User;
import com.karos.project.model.vo.NoteVo;
import com.karos.project.service.NoteService;
import com.karos.project.service.NotehistoryService;
import com.karos.project.service.NotethumbrecordsService;
import com.karos.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/note")
public class NoteController {
    @Resource
    NoteService noteService;
    @Resource
    QiniuServiceImpl qnsi;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    NotehistoryService notehistoryService;
    @Resource
    private UserService userService;
    @Resource
    private NotethumbrecordsService notethumbrecordsService;
    @Resource
    LockUtil lockUtil;

    @AuthCheck(mustRole = "admin")
    @GetMapping("/LockTest")
    public BaseResponse<String> test(@RequestParam("expTime") Long expTime){
        lockUtil.DistributedLock(RedisKeysConstant.ThumbsHistoryHash.intern(),expTime, TimeUnit.SECONDS);
        return ResultUtils.success("上锁成功，请在20s内进行测试操作");
    }
    @PostMapping("/thumb")
    public BaseResponse<Boolean> thumbNote(@RequestBody NoteDoThumbRequest noteDoThumbRequest, HttpServletRequest request){
        Notethumbrecords notethumbrecords = new Notethumbrecords();
        notethumbrecords.setNoteId(noteDoThumbRequest.getNoteId());
        notethumbrecords.setThumbTime(new Date());
        Boolean result = notethumbrecordsService.thumb(notethumbrecords, request);
        if (result==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(result);
    }
    /**
     * 创建
     *
     * @param noteAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addNote(@RequestBody NoteAddRequest noteAddRequest, HttpServletRequest request) {
        if (noteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //使用工具类，Nginx反向代理后仍为真实IP
        noteAddRequest.setIP(IpUtils.getIpAddr(request));
        //讲笔记内容存入七牛云
        String noteText = noteAddRequest.getNoteText();
        String noteUrl =null;
        try {
            File tempFile = File.createTempFile("temp",".html");
            FileOutputStream fos= new FileOutputStream(tempFile);
            //装饰者模式
            OutputStreamWriter osw=new OutputStreamWriter(fos,"utf-8");
            osw.write(noteText);
            osw.flush();
            osw.close();
            User loginUser = userService.getLoginUser(request);
            Long id = loginUser.getId();
            //防止日记重名，加上随机数
            long random = ((long)Math.random())%1000000+1;
            String fileName= DigestUtil.md5Hex(noteAddRequest.getNoteTitle()+ id+random);
            noteUrl=qnsi.uploadFile(tempFile, "noteFile", fileName, ".html", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Note note = new Note();
        BeanUtils.copyProperties(noteAddRequest, note);
        // 校验
        note.setCreateTime(new Date());
        note.setNoteUrl(noteUrl);
        String newNoteId=null;
        synchronized (note.getNoteUrl().intern()) {
            noteService.validNote(note, true);
            User loginUser = userService.getLoginUser(request);
            note.setUserId(loginUser.getId());
            boolean result = noteService.save(note);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            newNoteId = note.getId();
            //添加第一条历史记录
            Notehistory notehistory = new Notehistory();
            notehistory.setId(newNoteId);
            notehistory.setNoteUrl(noteUrl);
            notehistory.setIp(note.getIP());
            notehistory.setVersion(1L);
            notehistory.setUpdateTime(new Date());
            notehistoryService.save(notehistory);
        }
        return ResultUtils.success(newNoteId,"添加成功");
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNote(@RequestBody DeleteRequest<String> deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || StringUtils.isAnyBlank(deleteRequest.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        String id = deleteRequest.getId();
        // 判断是否存在
        Note oldNote = noteService.getById(id);
        if (oldNote == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldNote.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //逻辑上删除
        boolean b = noteService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param noteUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateNote(@RequestBody NoteUpdateRequest noteUpdateRequest,
                                            HttpServletRequest request) {
        if (noteUpdateRequest == null || noteUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Note note = new Note();
        BeanUtils.copyProperties(noteUpdateRequest, note);
        String noteText = noteUpdateRequest.getNoteText();
        // 参数校验
        noteService.validNote(note, false);
        User user = userService.getLoginUser(request);
        String id = noteUpdateRequest.getId();
        // 判断是否存在
        Note oldNote = noteService.getById(id);
        if (oldNote == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldNote.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        String oldnoteUrl = oldNote.getNoteUrl();
        String originName = qnsi.getOriginName(oldnoteUrl);
        String noteUrl=null;
        try {
            File tempFile = File.createTempFile("temp",".html");
            FileOutputStream fos= new FileOutputStream(tempFile);
            //装饰者模式
            OutputStreamWriter osw=new OutputStreamWriter(fos,"utf-8");
            osw.write(noteText);
            osw.flush();
            osw.close();
            noteUrl=qnsi.uploadFile(tempFile, "noteFile", originName, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        todo
        note.setNoteUrl(noteUrl);
        Notehistory notehistory = new Notehistory();
        notehistory.setId(note.getId());
        notehistory.setNoteUrl(noteUrl);
        notehistory.setIp(IpUtils.getIpAddr(request));
        QueryWrapper<Notehistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",note.getId());
        notehistory.setVersion(notehistoryService.count(queryWrapper)+1);
        notehistoryService.validNote(notehistory,true);
        boolean result = noteService.updateById(note)&&notehistoryService.save(notehistory);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<NoteVo> getNoteById(String id) {
        if (StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Note note = noteService.getById(id);
        NoteVo noteVo = new NoteVo();
        BeanUtils.copyProperties(note, noteVo);
        return ResultUtils.success(noteVo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param noteQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<Note>> listNote(NoteQueryRequest noteQueryRequest) {
        Note noteQuery = new Note();
        if (noteQueryRequest != null) {
            BeanUtils.copyProperties(noteQueryRequest, noteQuery);
        }
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>(noteQuery);
        List<Note> noteList = noteService.list(queryWrapper);
        return ResultUtils.success(noteList);
    }

    /**
     * 分页获取列表
     *
     * @param noteQueryRequest
     * @param request
     * @return
     */
    @AuthCheck
    @GetMapping("/list/page")
    public BaseResponse<Page<NoteVo>> listNoteByPage(NoteQueryRequest noteQueryRequest, HttpServletRequest request) {
        if (noteQueryRequest == null||(ObjectUtil.isNull(noteQueryRequest.getUserId())&&StringUtils.isAnyBlank(noteQueryRequest.getNoteTitle()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Note noteQuery = new Note();
        BeanUtils.copyProperties(noteQueryRequest, noteQuery);
        long current = noteQueryRequest.getCurrent();
        long size = noteQueryRequest.getPageSize();
        String sortField = noteQueryRequest.getSortField();
        String sortOrder = noteQueryRequest.getSortOrder();
        //模糊查询字段
        // content 需支持模糊搜索
        Long userId = noteQuery.getUserId();
        String noteTitle = noteQuery.getNoteTitle();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //ToDo: 直接查询数据库效率低 后期改进为ElasticSearch
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(Note.class,info -> !"IP".equals(info.getColumn()));
        Long loginId = userService.getLoginUser(request).getId();
        queryWrapper.like(StringUtils.isNotBlank(noteTitle),"noteTitle",noteTitle)
                        .eq(StringUtils.isNotBlank(noteTitle),"isPublic",1)
                                .or()
                                     .like(StringUtils.isNotBlank(noteTitle),"noteTitle",noteTitle)
                                        .eq(ObjectUtil.isEmpty(userId),"userId", loginId)
                .eq(ObjectUtil.isNotEmpty(userId),"userId",userId)
                                                .eq(StringUtils.isNotBlank(noteTitle),"isPublic",0);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC), sortField);
        Page<Note> notePage = noteService.page(new Page<>(current, size), queryWrapper);
        HashOperations hashOperations = redisTemplate.opsForHash();
        //从缓存里面读取
        ArrayList<Notethumbrecords> list = (ArrayList<Notethumbrecords>) hashOperations.get(RedisKeysConstant.ThumbsHistoryHash, String.valueOf(userId!=null?userId:loginId));
        Page<NoteVo> noteVoPage= (Page<NoteVo>) notePage.convert(u->{
            NoteVo v=new NoteVo();
            BeanUtils.copyProperties(u,v);
            Boolean thumb=false;
            if (ObjectUtil.isNotEmpty(list)){
                Iterator<Notethumbrecords> iterator = list.iterator();
                while(iterator.hasNext()){
                    Notethumbrecords next = iterator.next();
                    if (next.getNoteId().equals(v.getId())){
                        thumb=true;
                        break;
                    }
                }
            }
            v.setHasThumb(thumb);
            if (hashOperations.hasKey(RedisKeysConstant.ThumbsNum,v.getId()))
                v.setThumbNum(Long.valueOf((Integer)hashOperations.get(RedisKeysConstant.ThumbsNum,v.getId())));
            return v;
        });
        return ResultUtils.success(noteVoPage);
    }

    // endregion

}