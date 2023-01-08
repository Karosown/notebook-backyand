/**
 * Title
 *
 * @ClassName: CheckCodeParam
 * @Description:
 * @author: Karos
 * @date: 2022/12/16 21:07
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.model.dto.checkcode;

import com.karos.project.annotation.AllLimitCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckCodeRequest {
    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private String dateStamp;
    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String userMail;
    /**
     * 图形验证码
     */
    @Schema(description = "图形验证码")
    private String checkCode;
}
