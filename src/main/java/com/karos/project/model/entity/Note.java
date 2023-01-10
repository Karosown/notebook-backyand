package com.karos.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 日记表
 * @TableName note
 */
@TableName(value ="note")
@Data
public class Note implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 用户日记ID
     */
    private Long userNoteid;
    /**
     * 日记标题
     */
    private String noteTitle;
    /**
     * 日记地址
     */
    private String noteUrl;
    /**
     * 日记简介
     */
    private String noteIntroduction;
    /**
     * 是否公开（0为否，1为公开）
     */
    private Integer isPublic;

    /**
     * 浏览量
     */
    private Long viewNum;

    /**
     * IP地址
     */
    private String IP;

    /**
     * 点赞量
     */
    private Long thumbNum;

    /**
     * 注册时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date upDateTime;

    /**
     * 是否删除（0否，1是）
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}