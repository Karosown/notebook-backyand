/**
 * Title
 *
 * @ClassName: UserUpdatePasswordRequest
 * @Description:
 * @author: Karos
 * @date: 2022/12/22 15:19
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatePasswordRequest implements Serializable {
    /**
     * id
     */
    @Schema(defaultValue = "用户ID")
    private Long id;
    /**
     * 密码
     */
    private String userPassword;

    private String checkPassword;

    private String checkCode;

    private String userMail;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
