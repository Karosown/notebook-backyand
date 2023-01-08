package com.karos.project.model.dto.note;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class NoteUpdateRequest implements Serializable {
    /**
     * id
     */
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
     * 日记内容
     */
    private String noteText;

    /**
     * 是否公开（0为否，1为公开）
     */
    private Integer isPublic;



    private static final long serialVersionUID = 1L;
}