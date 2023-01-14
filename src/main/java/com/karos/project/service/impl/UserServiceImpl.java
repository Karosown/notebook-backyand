package com.karos.project.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karos.project.common.ErrorCode;
import com.karos.project.exception.BusinessException;
import com.karos.project.mapper.UserMapper;
import com.karos.project.model.dto.user.UserRegisterRequest;
import com.karos.project.model.entity.User;
import com.karos.project.service.UserService;
import com.karos.KaTool.io.ImageUtils;
import com.karos.KaTool.qiniu.impl.QiniuServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.NumberUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.karos.project.constant.UserConstant.ADMIN_ROLE;
import static com.karos.project.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 *
 * @author karos
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private QiniuServiceImpl qnsi;
    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "karos";

    @Override
    public String getUserAccount(Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(id))queryWrapper.eq("id",id);
        User user = userMapper.selectOne(queryWrapper);
        return user.getUserName();
    }

    @Override
    public String getUserName(String userAccount, Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userAccount))queryWrapper.eq("userAccount",userAccount);
        if (ObjectUtils.isNotEmpty(id))queryWrapper.eq("id",id);
        User user = userMapper.selectOne(queryWrapper);
        return user.getUserName();
    }

    @Override
    public String getUserAvatar(String userAccount,Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userAccount))queryWrapper.eq("userAccount",userAccount);
        if (ObjectUtils.isNotEmpty(id))queryWrapper.eq("id",id);
        User user = userMapper.selectOne(queryWrapper);
        return user.getUserAvatar();
    }
    @Override
    public String HexPassWord(CharSequence userPassword){
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


    /**
     * 在getLoginUser中会生成缓存，为此解决缓存导致更新后前台无法及时显示的问题
     * @param userId
     * @param request 如果为空则不清楚登录状态，只清除缓存
     * @return
     */
    @Override
    public boolean removeLoginUser(long userId,HttpServletRequest request) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        Long aLong = userId;
        if (!hashOperations.hasKey("LoginUser",aLong.toString())) {
            return false;
        }
        hashOperations.delete("LoginUser", aLong.toString());
        if (request!=null)request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public long userRegister(UserRegisterRequest user) {
        // 1. 校验
        String userAccount=user.getUserAccount();
        CharSequence userPassword=user.getUserPassword();
        CharSequence checkPassword=user.getCheckPassword();
        String userAvatar=user.getUserAvatar();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (StringUtils.isAnyBlank(userAvatar)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先上传一张头像来检测您的颜值");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            String userMail = user.getUserMail();
            queryWrapper.eq("userAccount", userAccount)
                    .or()
                    .eq("userMail", userMail);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复或者您的邮箱重复使用");
            }
            // 2. 加密
            String encryptPassword = HexPassWord(userPassword);
            // 3. 头像上传七牛云
            String logo=null;
            File tempFile= null;
            try {
                //base64转file
                tempFile = ImageUtils.base642img(userAvatar);
                logo = qnsi.uploadFile(tempFile, "/userAvatar",DigestUtil.md5Hex(userAccount), ".png",true);
            } catch (Exception e) {
                String basedir = qnsi.getBasedir()==null?"":qnsi.getBasedir();
                logo = "http://" + qnsi.getDomain()+basedir + "/"  +DigestUtil.md5Hex(userAccount)+".png"+"?datestamp="+new Date().getTime();
                e.printStackTrace();
            }
            // 4. 插入数据
            User usert = new User();
            usert.setUserName(user.getUserName());
            usert.setUserAccount(userAccount);
            usert.setUserAvatar(logo);
            usert.setUserPassword(encryptPassword);
            usert.setUserMail(userMail);
            usert.setCreateTime(new Date());
            boolean saveResult = this.save(usert);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return usert.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute( USER_LOGIN_STATE, user);
        return user;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        HashOperations hashOperations = redisTemplate.opsForHash();
        String UID = Long.valueOf(userId).toString();
        if (hashOperations.hasKey("LoginUser", UID)) {
            currentUser= (User) hashOperations.get("LoginUser", UID);
        }
        else {
            currentUser = this.getById(userId);
            if (currentUser!=null) {
                hashOperations.put("LoginUser", UID,currentUser);
                redisTemplate.expire("LoginUser",1, TimeUnit.HOURS);
            }

        }
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().setAttribute(USER_LOGIN_STATE,currentUser);
        return currentUser;
    }

    @Override
    public boolean isLogin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        return ObjectUtils.isNotEmpty(userObj);
    }

    /**
     * 是否为管理员
     *l
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //移除缓存
        Long id = getLoginUser(request).getId();
        redisTemplate.opsForHash().delete("LoginUser",id.toString());
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

}




