package com.karos.project.service;

import com.karos.project.model.entity.Notethumbrecords;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 30398
* @description 针对表【notethumbrecords(笔记点赞记录表)】的数据库操作Service
* @createDate 2023-01-05 04:22:14
*/
public interface NotethumbrecordsService extends IService<Notethumbrecords> {

    Boolean thumb(Notethumbrecords entity, HttpServletRequest request);
}
