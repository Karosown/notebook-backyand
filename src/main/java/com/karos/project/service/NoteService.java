package com.karos.project.service;

import com.karos.project.model.dto.note.NoteAddRequest;
import com.karos.project.model.entity.Note;
import com.baomidou.mybatisplus.extension.service.IService;
import com.karos.project.model.entity.Post;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 30398
* @description 针对表【note(日记表)】的数据库操作Service
* @createDate 2022-12-27 03:48:26
*/
public interface NoteService extends IService<Note> {
    /**
     * 校验
     *
     * @param note
     * @param add 是否为创建校验
     */
    void validNote(Note note, boolean add);

    void validNote(NoteAddRequest note, boolean add);

}
