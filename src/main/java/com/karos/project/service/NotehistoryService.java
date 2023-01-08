package com.karos.project.service;

import com.karos.project.model.entity.Note;
import com.karos.project.model.entity.Notehistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 30398
* @description 针对表【notehistory(笔记历史)】的数据库操作Service
* @createDate 2022-12-27 18:39:39
*/
public interface NotehistoryService extends IService<Notehistory> {
    //Todo：笔记更新、版本记录
    void validNote(Notehistory notehistory, boolean add);
}
