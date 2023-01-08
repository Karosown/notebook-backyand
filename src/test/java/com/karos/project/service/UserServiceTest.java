package com.karos.project.service;

import com.karos.project.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 用户服务测试
 *
 * @author karos
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;


    @Test
    void testAddUser() {
        User user = new User();
        user.setUserName("q");
        user.setUserPassword("q");
        user.setUserAccount("123");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    @Test
    void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    @Test
    void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    void userRegister() {
        String userAccount = "karos";
        String userPassword = "";
        String checkPassword = "123456";
        try {
//            long result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            userAccount = "yu";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            userAccount = "karos";
            userPassword = "123456";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            userAccount = "yu pi";
            userPassword = "12345678";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            checkPassword = "123456789";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            userAccount = "dogkaros";
            checkPassword = "12345678";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
            userAccount = "karos";
//            result = userService.userRegister(userAccount, userPassword, checkPassword,"Karos",null);
//            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }
}