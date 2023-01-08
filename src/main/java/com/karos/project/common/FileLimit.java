/**
 * Title
 *
 * @ClassName: FileLimit
 * @Description:
 * @author: Karos
 * @date: 2022/12/18 15:11
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileLimit {
    /**
     * 限制类型
     */
    String Name;
    /**
     * IP地址和对应的次数
     */
    ConcurrentHashMap<String,IPFileDownloadNum> ipFileDownloadNums;
}
