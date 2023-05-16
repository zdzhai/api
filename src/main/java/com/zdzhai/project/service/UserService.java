package com.zdzhai.project.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.model.dto.user.UserRegisterRequest;
import com.zdzhai.project.model.vo.AkVO;
import com.zdzhai.project.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务
 *
 * @author dongdong
 */
public interface UserService extends IService<User> {

    /**
     *
     * @param userRegisterRequest
     * @param request
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest,
                      HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request, HttpServletResponse response);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request, HttpServletResponse response);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 生成图形验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 向手机号发送短信验证码
     * @param mobile
     * @return
     */
    String messageCaptcha(String mobile);

    /**
     * 返回用户的ak信息
     * @param id
     * @param request
     * @param response
     * @return
     */
    BaseResponse<AkVO> getAkByUserId(Long id, HttpServletRequest request,
                                       HttpServletResponse response);

    /**
     * 获取GitHub上这个项目的stars
     * @return
     */
    String getGithubStars();

    /**
     * 获取echarts需要展示的数据
     * @return
     */
    List<Object> getEchartsData();
}
