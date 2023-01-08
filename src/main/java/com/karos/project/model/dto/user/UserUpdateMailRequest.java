/**
 * Title
 *
 * @ClassName: UserUpdateMailRequest
 * @Description:
 * @author: 巫宗霖
 * @date: 2022/12/22 15:29
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
@Data
public class UserUpdateMailRequest implements Serializable {
    /**
     * id
     */
    @Schema(defaultValue = "用户ID")
    private Long id;
    /**
     * 密码
     */
    private String userNewMail;
    private String userMail;
    private String checkCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
