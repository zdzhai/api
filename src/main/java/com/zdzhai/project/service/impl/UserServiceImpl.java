package com.zdzhai.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.common.CookieConstant;
import com.zdzhai.project.common.CookieUtils;
import com.zdzhai.project.common.ErrorCode;
import com.zdzhai.project.common.TokenUtils;
import com.zdzhai.project.constant.UserConstant;
import com.zdzhai.project.exception.BusinessException;
import com.zdzhai.project.mapper.UserMapper;
import com.zdzhai.project.model.vo.LoginUserVO;
import com.zdzhai.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.protocol.tri.call.UnaryServerCallListener;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zdzhai.project.constant.UserConstant.*;


/**
 * 用户服务实现类
 *
 * @author dongdong
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zhai";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
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
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            //3.分配 accessKey secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
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
        // 3. 初始化用户的登录态
        return initUserLogin(user,response);
    }

    private LoginUserVO initUserLogin(User user, HttpServletResponse response) {
        //把用户信息放到redis中
        User safetyUser = getSafetyUser(user);
        Map<String, Object> userMap = BeanUtil.beanToMap(safetyUser, true,true);
        userMap.forEach((key, value) -> {
            if (null != value) {
                userMap.put(key, String.valueOf(value));
            }
        });

        String userKey = API_USER_ID + safetyUser.getId();
        stringRedisTemplate.opsForHash().putAll(userKey,userMap);
        stringRedisTemplate.opsForHash().getOperations().expire(userKey,720, TimeUnit.HOURS);

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user,loginUserVO);
        TokenUtils tokenUtils = new TokenUtils();
        //生成token
        String token = tokenUtils.createToken(user.getId().toString(), loginUserVO.getUserAccount());
        loginUserVO.setToken(token);
        Cookie cookie = new Cookie(CookieConstant.headAuthorization, token);
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        //向响应中添加带有token的Cookie
        response.addCookie(cookie);
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVO.getId().toString(), loginUserVO.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        //向响应中添加用户记住登录的密钥
        cookie.setMaxAge(CookieConstant.expireTime);
        response.addCookie(cookie1);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request, HttpServletResponse response) {
        //1. 先判断是否已登录 从redis中取
        //有则返回
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0){
            return null;
        }
        String remember = null;
        String authorization = null;
        for ( Cookie cookie : cookies){
            String name = cookie.getName();
            if (CookieConstant.headAuthorization.equals(name)){
                authorization = cookie.getValue();
            }
            if (CookieConstant.autoLoginAuthCheck.equals(name)){
                remember = cookie.getValue();
            }
        }
        //token 信息有问题则抛异常
        if (authorization == null || remember == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        CookieUtils cookieUtils = new CookieUtils();
        String[] strings = cookieUtils.decodeAutoLoginKey(remember);
        if (strings.length!=3){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR,"请重新登录");
        }
        String sId = strings[0];
        String sUserAccount = strings[1];
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(API_USER_ID + sId);
        if (userMap != null){
            User user = BeanUtil.mapToBean(userMap, User.class, true, CopyOptions.create()
                    .setIgnoreNullValue(true)
                    .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
            return user;
        } else {
            //2. 没有则判断token信息
            JWT jwt = JWTUtil.parseToken(authorization);
            String id = (String) jwt.getPayload("id");
            String userAccount = (String) jwt.getPayload("userAccount");
            if (!sId.equals(id) || !sUserAccount.equals(userAccount)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"请重新登录");
            }
            //没问题则调用initUserLogin
            User userById = this.getById(id);
            userById.setUserPassword(null);
            LoginUserVO loginUserVO = initUserLogin(userById, response);
            User user = new User();
            BeanUtil.copyProperties(loginUserVO,user);
            return user;
        }
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request, HttpServletResponse response) {
        // 仅管理员可查询
        User loginUser = getLoginUser(request, response);
        return loginUser != null && ADMIN_ROLE.equals(loginUser.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (getLoginUser(request,response) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        return true;
    }

    /**
     * 脱敏用户信息
     * @param user
     * @return
     */
    public User getSafetyUser(User user){
        User safetyUser = new User();
        BeanUtil.copyProperties(user,safetyUser);
        safetyUser.setUserPassword(null);
        safetyUser.setAccessKey(null);
        safetyUser.setSecretKey(null);
        return safetyUser;
    }

}




