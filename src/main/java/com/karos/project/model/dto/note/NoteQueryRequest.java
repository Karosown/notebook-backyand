package com.karos.project.model.dto.note;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.karos.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author karos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoteQueryRequest extends PageRequest implements Serializable {


    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 日记标题
     */
    private String noteTitle;
    private static final long serialVersionUID = 1L;
}