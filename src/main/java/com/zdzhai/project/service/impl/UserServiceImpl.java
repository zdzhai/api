package com.zdzhai.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.common.ResultUtils;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.common.*;

import com.zdzhai.project.mapper.InterfaceInfoMapper;
import com.zdzhai.project.mapper.UserMapper;
import com.zdzhai.project.model.dto.SmsDTO;
import com.zdzhai.project.model.dto.user.UserRegisterRequest;
import com.zdzhai.project.model.vo.AkVO;
import com.zdzhai.project.model.vo.EchartsVO;
import com.zdzhai.project.model.vo.LoginUserVO;
import com.zdzhai.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    SmsLimiter smsLimiter;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zhai";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest,
                             HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String mobile = userRegisterRequest.getMobile();
        String code = userRegisterRequest.getCode();
        String captcha = userRegisterRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword,
                checkPassword, mobile, code, captcha)) {
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
        CheckPhoneNumber checkPhoneNumber = new CheckPhoneNumber();
       if( !checkPhoneNumber.isPhoneNum(mobile)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
       }
        String signature = request.getHeader("signature");
        if (signature == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String redisCaptcha = stringRedisTemplate.opsForValue().get(API_CAPTCHA_ID + signature);
        if (redisCaptcha == null || checkPhoneNumber.isCaptcha(captcha) ||   !captcha.equals(redisCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图形验证码错误或已经过期，请重新刷新验证码");
        }
        // 手机号和验证码是否匹配
        boolean isVerify = smsLimiter.verifyCode(mobile, code);
        if (!isVerify){
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            String selectMobileNum = userMapper.selectMobileNum(mobile);
            if (selectMobileNum != null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号已被注册");
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
        response.setHeader("Access-Control-Allow-Credentials", "true");
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVO.getId().toString(), loginUserVO.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        //向响应中添加用户记住登录态的密钥
        cookie.setMaxAge(CookieConstant.expireTime);
        response.addCookie(cookie1);
        response.setHeader("Access-Control-Allow-Credentials", "true");
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
            userById.setSecretKey(null);
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

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        //1. 生成随机数以及颜色验证码图片
        //2. 通过response响应到浏览器
        //3. 前端携带一个唯一标识，用于标识不同用户，进行判断，也可以一防止盗刷
        //4. 存储验证码到redis，用于验证
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        //定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        //在前端发起请求时携带一个captchaId，用于标识不同的用户
        String signature = request.getHeader("signature");
        if (signature == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try{
            //设置验证码
            lineCaptcha.setGenerator(randomGenerator);
            //图形验证码写出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature,lineCaptcha.getCode());
            //关闭流
            response.getOutputStream().close();
            //将验证码存到redis中，有效期为2min
            stringRedisTemplate.opsForValue().set(API_CAPTCHA_ID+signature, lineCaptcha.getCode(),2,TimeUnit.MINUTES);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String messageCaptcha(String mobile) {
        if (mobile == null || "".equals(mobile)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CheckPhoneNumber checkPhoneNumber = new CheckPhoneNumber();
        //验证手机号是否合法
        // 生成验证码，
        // 用redis保存手机号和验证码,并使用后令牌桶算法实现发送控制
        //调用第三方去发送验证码
        boolean isPhoneNum = checkPhoneNumber.isPhoneNum(mobile);
        if (!isPhoneNum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号非法");
        }
         int messageCode = (int) ((Math.random() * 9 + 1) * 10000);
        boolean isSend = smsLimiter.sendSmsAuth(mobile, String.valueOf(messageCode));
        if (!isSend){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"操作频繁，请稍后重试");
        }
        SmsDTO smsDTO = new SmsDTO(mobile, String.valueOf(messageCode));
        //todo 实际发送短信的功能交给第三方服务去实现
        return String.valueOf(messageCode);
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
        safetyUser.setSecretKey(null);
        safetyUser.setUpdateTime(null);
        return safetyUser;
    }
    /**
     * 根据用户id获取用户的密钥
     * @param id
     * @param request
     * @return
     */
    @Override
    public BaseResponse<AkVO> getAkByUserId(Long id, HttpServletRequest request,
                                              HttpServletResponse response) {
        if (id <=0 || id ==null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 先判断是否已登录
        User currentUser = getLoginUser(request, response);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (!currentUser.getId().equals(id)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        User user = userMapper.selectById(id);
        if(null == user){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        AkVO authVO = new AkVO();
        authVO.setAccesskey(user.getAccessKey());
        return ResultUtils.success(authVO);
    }

    /**
     * 获取GitHub上这个项目的stars
     * @return
     */
    @Override
    public String getGithubStars() {
        String listContent = null;
        try {
            listContent= HttpUtil.get("https://img.shields.io/github/stars/zdzhai?style=social");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取GitHub Starts 超时");
        }
        //该操作查询时间较长
        List<String> titles = ReUtil.findAll("<title>(.*?)</title>", listContent, 1);
        String stars = null;
        for (String title : titles) {
            //打印标题
            String[] split = title.split(":");
            stars = split[1];
        }
        return stars;
    }

    /**
     * 获取echarts需要展示的数据
     * @return
     */
    @Override
    public List<Object> getEchartsData() {
        //1、获取最近7天的日期
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            Date date = DateUtils.addDays(new Date(), -i);
            String formatDate = sdf.format(date);
            dateList.add(formatDate);
        }
        ArrayList<Object> objects = new ArrayList<>();
        //3、根据最近七天的日期去数据库中查询用户信息
        ArrayList<Long> userList = extracted(dateList, userMapper.getUserList(dateList),false);
        //4、查询最近7天的接口信息
        ArrayList<Long> interfaceList = extracted(dateList, interfaceInfoMapper.getInterfaceList(dateList),false);
        Collections.reverse(dateList);
        objects.add(dateList);
        objects.add(userList);
        objects.add(interfaceList);
        return objects;
    }

    /**
     * 封装echarts返回数据
     * @param dateList
     * @param list
     * @return
     */
    private static ArrayList<Long> extracted(List<String> dateList, List<EchartsVO> list,boolean isChange) {
        ArrayList<Long> echartsVos = new ArrayList<>();
        for (int i=0;i<7;i++){
            boolean bool=false;
            //创建内循环 根据查询出已有的数量 循环次数
            for (int m = 0; m< list.size(); m++){
                if (!isChange){
                    EchartsVO echartsVo = list.get(m);
                    if (dateList.get(i).equals(echartsVo.getDate())){
                        echartsVos.add(echartsVo.getCount());
                        bool=true;
                        break;
                    }
                }else {
                    //处理数据转化问题
                    String s = JSONUtil.toJsonStr(list.get(m));
                    EchartsVO echartsVo = JSONUtil.toBean(s, EchartsVO.class);
                    if (dateList.get(i).equals(echartsVo.getDate())){
                        echartsVos.add(echartsVo.getCount());
                        bool=true;
                        break;
                    }
                }
            }
            if (!bool) {
                echartsVos.add(0L);
            }
        }
        Collections.reverse(echartsVos);
        return echartsVos;
    }
}




