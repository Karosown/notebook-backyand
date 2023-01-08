package com.karos.project.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author karos
 */
@Data
public class DeleteRequest<T> implements Serializable {
    /**
     * id
     */
    private T id;

    private static final long serialVersionUID = 1L;
}