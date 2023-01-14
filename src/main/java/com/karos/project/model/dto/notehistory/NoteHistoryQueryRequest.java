package com.karos.project.model.dto.notehistory;

import com.karos.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author karos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoteHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private String id;

    private static final long serialVersionUID = 1L;
}