package com.karos.project.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.karos.project.model.entity.Note;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 日记表
 * @TableName note
 */
@Data
public class NoteVo implements Serializable {
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
     * 是否公开（0为否，1为公开）
     */
    private Integer isPublic;

    /**
     * 浏览量
     */
    private Long viewNum;


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
     * 是否已点赞
     */
    private Boolean hasThumb;

    private static final long serialVersionUID = 1L;
}