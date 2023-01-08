package com.karos.project.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author karos
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    @Schema(defaultValue = "用户ID")
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

//    /**
//     * 账号
//     */
//    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

//    /**
//     * 性别
//     */
//    private Integer gender;

//    /**
//     * 用户角色: user, admin
//     */
//    private String userRole;

//    /**
//     * 密码
//     */
//    private String userPassword;
//
//    /**
//     * 用户邮箱
//     */
//    private String userMail;
//    /**
//     * 验证码
//     */
//    private String checkCode;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}