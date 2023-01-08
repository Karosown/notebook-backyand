/**
 * Title
 *
 * @ClassName: EmailUtilsTests
 * @Description:
 * @author: Karos
 * @date: 2022/12/14 21:52
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailUtilsTests {
    @Autowired
    private EmailUtils eMailTools;

    @Test
    void testEmailtools(){
        eMailTools.setMessage("66985726@qq.com","短信测试","短信测试");
        eMailTools.send();
    }
}
