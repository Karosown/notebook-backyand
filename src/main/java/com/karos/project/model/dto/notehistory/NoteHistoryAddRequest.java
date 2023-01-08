package com.karos.project.model.dto.notehistory;

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
public class NoteHistoryAddRequest implements Serializable {

    /**
     * 日记id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 日记文本 - Html格式
     */
    private String noteText;

    /**
     * 日记
     */
    private Long version;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}