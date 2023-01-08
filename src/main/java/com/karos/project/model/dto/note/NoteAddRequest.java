package com.karos.project.model.dto.note;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class NoteAddRequest implements Serializable {


    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 日记标题
     */
    private String noteTitle;
    /**
     * 日记内容
     */
    private String noteText;

    /**
     * 是否公开（0为否，1为公开）
     */
    private Integer isPublic;


    /**
     * IP地址
     */
    private String IP;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}