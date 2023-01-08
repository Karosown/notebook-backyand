/**
 * Title
 *
 * @ClassName: FinalvarConfig
 * @Description:
 * @author: 巫宗霖
 * @date: 2022/12/15 14:02
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("final")
public class FinalvarConfig {
    public long EXP_TIME;
    public long MAX_FERQUENCY;
}
