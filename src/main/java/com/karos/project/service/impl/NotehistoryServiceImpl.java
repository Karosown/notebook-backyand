package com.karos.project.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karos.project.common.ErrorCode;
import com.karos.project.exception.BusinessException;
import com.karos.project.model.entity.Notehistory;
import com.karos.project.service.NotehistoryService;
import com.karos.project.mapper.NotehistoryMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 30398
* @description 针对表【notehistory(笔记历史)】的数据库操作Service实现
* @createDate 2022-12-27 18:39:39
*/
@Service
public class NotehistoryServiceImpl extends ServiceImpl<NotehistoryMapper, Notehistory>
    implements NotehistoryService {

    @Override
    public void validNote(Notehistory notehistory, boolean add) {
        if (notehistory==null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
     String id = notehistory.getId();
     String noteUrl = notehistory.getNoteUrl();
     String ip = notehistory.getIp();
     Long version = notehistory.getVersion();
        if (add){
            if (StringUtils.isAnyBlank(id,noteUrl,ip)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (ObjectUtils.anyNull(version)){
                QueryWrapper<Notehistory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("id",id);
                version=this.count(queryWrapper)+1;
            }
        }

    }
}




