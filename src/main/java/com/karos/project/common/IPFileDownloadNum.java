/**
 * Title
 *
 * @ClassName: IPFileDownloadNum
 * @Description:
 * @author: Karos
 * @date: 2022/12/15 13:46
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

import com.karos.project.annotation.AllLimitCheck;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 文件上传限制类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IPFileDownloadNum {
//    /**
//     * IP地址
//     */
//    String IP;
    /**
     * 上传次数
     */
    Long freQuency;
    /**
     * 过期时间
     */
    Date expTime;

    public IPFileDownloadNum inc(){
        this.freQuency++;
        return this;
    }
}
