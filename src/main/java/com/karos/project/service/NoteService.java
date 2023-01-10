package com.karos.project.service;

import com.karos.project.common.RegexValid;
import com.karos.project.model.dto.note.NoteAddRequest;
import com.karos.project.model.dto.note.NoteUpdateRequest;
import com.karos.project.model.entity.Note;
import com.baomidou.mybatisplus.extension.service.IService;
import com.karos.project.model.entity.Post;
import org.apache.commons.lang3.RegExUtils;

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

    default String getIntroduction(String data){
        data=data.replaceAll(RegexValid.scriptRegex, "");
        data=data.replaceAll(RegexValid.styleRegex,"");
        data=data.replaceAll(RegexValid.htmlRegex,"");
        data=data.replaceAll(RegexValid.spaceRegex,"");

        if (data.length()>50){
            data=data.substring(0,50);
        }
        return data+"...";
    }

    default String getIntroduction(NoteAddRequest noteAddRequest){
        return getIntroduction(noteAddRequest.getNoteText());
    }
    default String getIntroduction(NoteUpdateRequest updateRequest){
        return getIntroduction(updateRequest.getNoteText());
    }

}
