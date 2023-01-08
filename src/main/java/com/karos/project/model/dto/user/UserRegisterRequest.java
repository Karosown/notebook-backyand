package com.karos.project.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author karos
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String userName;

    private String userMail;
    @Schema(defaultValue = "用户头像(base64编码)")
    private String userAvatar;

    private String checkCode;
}
