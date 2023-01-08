package com.karos.project.service.impl;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karos.project.common.ErrorCode;
import com.karos.project.exception.BusinessException;
import com.karos.project.model.dto.note.NoteAddRequest;
import com.karos.project.model.entity.Note;
import com.karos.project.model.entity.Post;
import com.karos.project.model.enums.PostGenderEnum;
import com.karos.project.model.enums.PostReviewStatusEnum;
import com.karos.project.service.NoteService;
import com.karos.project.mapper.NoteMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 30398
* @description 针对表【note(日记表)】的数据库操作Service实现
* @createDate 2022-12-27 03:48:26
*/
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note>
    implements NoteService{
    @Resource
    private NoteMapper noteMapper;
    @Override
    public void validNote(Note note, boolean add) {

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String id = note.getId();
        Long userId = note.getUserId();
        Long userNoteid = note.getUserNoteid();
        String noteTitle = note.getNoteTitle();
        String noteUrl = note.getNoteUrl();
        Integer isPublic = note.getIsPublic();
//        ip可以为空，再controller中进行获取
//        String iP = note.getIP();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(noteTitle,noteUrl) || ObjectUtils.anyNull(userId,isPublic)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (ObjectUtils.anyNull(userNoteid)){
                QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId",userId);
                note.setUserNoteid(this.count(queryWrapper)+1);
            }
        }
    }

    @Override
    public void validNote(NoteAddRequest note, boolean add) {

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = note.getUserId();
        String noteTitle = note.getNoteTitle();
        String noteText = note.getNoteText();
        Integer isPublic = note.getIsPublic();
        String iP = note.getIP();

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(noteTitle,noteText,iP) || ObjectUtils.anyNull(userId,isPublic)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }



}




