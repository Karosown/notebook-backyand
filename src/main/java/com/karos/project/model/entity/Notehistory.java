package com.karos.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 笔记历史
 * @TableName notehistory
 */
@TableName(value ="notehistory")
@Data
public class Notehistory implements Serializable {
    /**
     * 日记id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 日记地址
     */
    private String noteUrl;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 日记
     */
    private Long version;
    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 是否删除（0否，1是）
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}