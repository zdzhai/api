package com.zdzhai.project.common;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.model.entity.thirdparty.vo.LoginUserVo;
import com.zdzhai.project.constant.UserConstant;
import com.zdzhai.project.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author dongdong
 *
 */
@Component
public class Oauth2LoginUtils {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TokenUtils tokenUtils;

    private static Oauth2LoginUtils oauth2LoginUtils;

    @PostConstruct
    public void init(){
        oauth2LoginUtils = this;
    }


    /**
     * 通过第三方进行登录后，注册用户信息
     * @param loginUserVo
     * @return
     */
    public LoginUserVo checkOauth2Login(LoginUserVo loginUserVo){
        String userAccount = loginUserVo.getUserAccount();
        String name = loginUserVo.getUserName();
        String userAvatar = loginUserVo.getUserAvatar();
        User user = oauth2LoginUtils.userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", userAccount));

        if (null != user){
            user.setUserPassword(null);
            String mobile = user.getMobile();
            String newMobile = "".equals(mobile) ? "" : mobile.substring(0, 3) + "****" + mobile.substring(7);
            user.setMobile(newMobile);
            BeanUtils.copyProperties(user,loginUserVo);
            String token = oauth2LoginUtils.tokenUtils.createToken(String.valueOf(loginUserVo.getId()),loginUserVo.getUserAccount());
            loginUserVo.setToken(token);
        }else {
            String accessKey = DigestUtil.md5Hex(UserConstant.SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(UserConstant.SALT + userAccount + RandomUtil.randomNumbers(8));
            User user1 = new User();
            user1.setUserAccount(userAccount);
            user1.setUserName(name);
            user1.setUserAvatar(userAvatar);
            user1.setAccessKey(accessKey);
            user1.setSecretKey(secretKey);
            oauth2LoginUtils.userMapper.insert(user1);
            // 进行登录操作
            User dbUser = oauth2LoginUtils.userMapper.selectById(user1.getId());
            BeanUtils.copyProperties(dbUser,loginUserVo);
            String token = oauth2LoginUtils.tokenUtils.createToken(String.valueOf(loginUserVo.getId()),loginUserVo.getUserAccount());
            loginUserVo.setToken(token);
        }
        return loginUserVo;
    }
}
