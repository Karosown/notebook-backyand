package com.karos.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 笔记点赞记录表
 * @TableName notethumbrecords
 */
@TableName(value ="notethumbrecords")
@Data
public class Notethumbrecords implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 笔记ID
     */
    private String noteId;

    /**
     * 点赞时间
     */
    private Date thumbTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notethumbrecords that = (Notethumbrecords) o;
        return userId.equals(that.userId) && noteId.equals(that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, noteId);
    }
}