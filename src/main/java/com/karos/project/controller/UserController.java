package com.karos.project.controller;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.karos.KaTool.io.ImageUtils;
import com.karos.project.annotation.AllLimitCheck;
import com.karos.project.common.BaseResponse;
import com.karos.project.common.DeleteRequest;
import com.karos.project.common.ErrorCode;
import com.karos.project.common.ResultUtils;
import com.karos.project.exception.BusinessException;
import com.karos.project.model.dto.user.*;
import com.karos.project.model.entity.User;
import com.karos.project.model.vo.UserVO;
import com.karos.project.service.UserService;
import com.karos.KaTool.qiniu.impl.QiniuServiceImpl;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author karos
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private QiniuServiceImpl qnsi;
    @Resource
    private RedisTemplate redisTemplate;
    // region 登录相关

    @GetMapping("/get/userAccount")
    public BaseResponse<String> getUserAccount(@RequestParam(value = "id",required = false) Long id,
                                            HttpServletResponse httpServletResponse){
        if (ObjectUtils.anyNull(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        String userAccount=null;
        hashOperations.get("UserAccountdb",id.toString());
        if (userAccount==null) {
            synchronized ("Lock".intern()){
                userAccount = userService.getUserAccount(id);
            }
            if (ObjectUtils.isNotEmpty(id))hashOperations.put("UserAccountdb",id.toString(),userAccount);
        }
        return ResultUtils.success(userAccount);
    }
    /**
     * 获取用户昵称
     * @param userAccount
     * @param httpServletResponse
     * @return
     */
    @GetMapping("/get/userName")
    public BaseResponse<String> getUserName(@RequestParam(value = "useraccount",required = false) String userAccount,
                                            @RequestParam(value = "id",required = false) Long id,
                                            HttpServletResponse httpServletResponse){
        if (StringUtils.isAnyBlank(userAccount)&& ObjectUtils.anyNull(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        String userName=null;
        if (StringUtils.isNotBlank(userAccount))userName= (String) hashOperations.get("UserNamedb",userAccount);
        if (userName==null) hashOperations.get("UserNamedb",id.toString());
        if (userName==null) {
            synchronized ("Lock"){
                userName = userService.getUserName(userAccount,id);
            }
            if (StringUtils.isNotBlank(userAccount))hashOperations.put("UserNamedb",userAccount,userName);
            if (ObjectUtils.isNotEmpty(id))hashOperations.put("UserNamedb",id.toString(),userName);
        }
        return ResultUtils.success(userName);
    }
    /**
     * 获取用户头像
     * @param userAccount
     * @param httpServletResponse
     * @return
     */
    @GetMapping("/get/userAvatar")
    public BaseResponse<String> getUserAvatar(@RequestParam(value = "useraccount",required = false) String userAccount,
                                              @RequestParam(value = "id",required = false)Long id, HttpServletResponse httpServletResponse){
        if (StringUtils.isAnyBlank(userAccount)&& ObjectUtils.anyNull(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        String userAvatar=null;
        if (StringUtils.isNotBlank(userAccount))userAvatar= (String) hashOperations.get("UserAvatardb",userAccount);
        if (userAvatar==null) hashOperations.get("UserAvatardb",id.toString());
        if (userAvatar==null) {
            synchronized ("Lock"){
                userAvatar = userService.getUserAvatar(userAccount,id);
            }
            if (StringUtils.isNotBlank(userAccount))hashOperations.put("UserAvatardb",userAccount,userAvatar);
            if (ObjectUtils.isNotEmpty(id))hashOperations.put("UserAvatardb",id.toString(),userAvatar);
        }
        try {
            ImageUtils.img2fileToOutputStream(userAvatar,httpServletResponse.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultUtils.success(userAvatar);
    }
    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @AllLimitCheck(mustText = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userMail = userRegisterRequest.getUserMail();
        String checkCode = userRegisterRequest.getCheckCode().toUpperCase();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,userMail,checkCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请先完善注册信息");
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (!checkCode.equals(hashOperations.get("checkcode_sms",userMail))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"邮箱验证码错误");
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest<Long> deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AllLimitCheck(mustText = "用户信息更新")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAllBlank(userUpdateRequest.getUserName(),userUpdateRequest.getUserAvatar())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"如果要修改，请您至少修改一项");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        String userAvatar = userUpdateRequest.getUserAvatar();
        //头像上传，当前端传过来头像的时候，传过来的不是null，是空字符串
        if (StringUtils.isNotBlank(userAvatar)){
            String userAccount =  userService.getLoginUser(request).getUserAccount();
            String logo=null;
            File tempFile= null;
            try {
                tempFile = ImageUtils.base642img(userAvatar);
                logo = qnsi.uploadFile(tempFile, "/userAvatar",DigestUtil.md5Hex(userAccount), ".png",true);
            } catch (Exception e) {
                String basedir = qnsi.getBasedir()==null?"":qnsi.getBasedir();
                logo = "http://" + qnsi.getDomain()  +basedir+'/' +DigestUtil.md5Hex(userAccount)+".png"+"?datestamp="+new Date().getTime();
                e.printStackTrace();
            }
            user.setUserAvatar(logo);
        }else{
            //前端传进来的头像为空字符串，这里把他重新改为Null
            user.setUserAvatar(null);
        }
        //同理，用户名一样的要判断
        if(StringUtils.isBlank(userUpdateRequest.getUserName())){
            user.setUserName(null);
        }
        boolean result = userService.updateById(user)&&userService.removeLoginUser(user.getId(),null);
        return ResultUtils.success(result);
    }
    @PostMapping("/update/resetpassword")
    @AllLimitCheck(mustText = "用户密码更新")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request) {
        if (userUpdatePasswordRequest == null || userUpdatePasswordRequest.getId() == null||StringUtils.isAnyBlank(
                userUpdatePasswordRequest.getUserPassword(),
                userUpdatePasswordRequest.getCheckPassword(),
                userUpdatePasswordRequest.getUserMail(),
                userUpdatePasswordRequest.getCheckCode()
        )) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请检查填写是否完整");
        }
        if (!userUpdatePasswordRequest.getUserPassword().equals(userUpdatePasswordRequest.getCheckPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不同，请检查");
        }
        if (!userService.getLoginUser(request).getUserMail().equals(userUpdatePasswordRequest.getUserMail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱地址不匹配，请检查");
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        String userMail = userUpdatePasswordRequest.getUserMail();
        String checkCode=userUpdatePasswordRequest.getCheckCode().toUpperCase(Locale.ROOT);
        if (!checkCode.equals(hashOperations.get("checkcode_sms",userMail))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"邮箱验证码错误");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdatePasswordRequest, user);
        user.setUserPassword(userService.HexPassWord(user.getUserPassword()));
        boolean result = userService.updateById(user)&&userService.removeLoginUser(user.getId(),request);
        return ResultUtils.success(result);
    }



    @PostMapping("/update/resetmail")
    @AllLimitCheck(mustText = "用户邮箱更新")
    public BaseResponse<Boolean> updateUserMail(@RequestBody UserUpdateMailRequest userUpdateMailRequest, HttpServletRequest request) {
        if (userUpdateMailRequest == null || userUpdateMailRequest.getId() == null||
                StringUtils.isAnyBlank(userUpdateMailRequest.getCheckCode(),userUpdateMailRequest.getUserMail(),
                        userUpdateMailRequest.getUserNewMail(),userUpdateMailRequest.getCheckCode())||
                !Validator.isEmail(userUpdateMailRequest.getUserMail())||!Validator.isEmail(userUpdateMailRequest.getUserNewMail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!userService.getLoginUser(request).equals(userUpdateMailRequest.getUserMail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱地址不匹配，请检查");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateMailRequest, user);
        HashOperations hashOperations = redisTemplate.opsForHash();
        String userMail = userService.getLoginUser(request).getUserMail();
        String checkCode=userUpdateMailRequest.getCheckCode();
        if (!checkCode.equals(hashOperations.get("checkcode_sms",userMail))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"邮箱验证码错误");
        }
        user.setUserMail(userUpdateMailRequest.getUserNewMail());
        boolean result = userService.updateById(user)&&userService.removeLoginUser(user.getId(),request);
        return ResultUtils.success(result);
    }
    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    // endregion
}
