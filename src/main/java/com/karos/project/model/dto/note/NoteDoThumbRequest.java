package com.karos.project.model.dto.note;

import lombok.Data;

import java.io.Serializable;

/**
 * 点赞 / 取消点赞请求
 *
 * @author karos
 */
@Data
public class NoteDoThumbRequest implements Serializable {

    /**
     * 笔记 id
     */
    private String noteId;

    private static final long serialVersionUID = 1L;
}