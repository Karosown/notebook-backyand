package com.karos.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.karos.project.model.dto.user.UserRegisterRequest;
import com.karos.project.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author karos
 */
public interface UserService extends IService<User> {
    String getUserAccount(Long id);
    String getUserName(String userAccount,Long id);
    /**
     * 获取用户头像地址
     * @param userAccount
     * @return
     */
    String getUserAvatar(String userAccount,Long id);

    String HexPassWord(CharSequence userPassword);

    /**
     * 在getLoginUser中会生成缓存，为此解决缓存导致更新后前台无法及时显示的问题
     * @param userId
     * @param request 如果为空则不清楚登录状态，只清除缓存
     * @return
     */
    boolean removeLoginUser(long userId,HttpServletRequest request);
    /**
     * 用户注册
     *
     * @param user  用户信息
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest user);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    boolean isLogin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);
}
