package com.karos.project.common;

import com.karos.KaTool.other.MethodIntefaceUtil;

/**
 * 返回工具类
 *
 * @author karos
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }
    public static <T> BaseResponse<T> success(T data,String message) {
        return new BaseResponse<>(0, data, message);
    }
    public static <T> BaseResponse<T> success(T data, MethodIntefaceUtil MethodIntefaceUtil) {
        return new BaseResponse<>(0, data, (String) MethodIntefaceUtil.method());
    }
    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
