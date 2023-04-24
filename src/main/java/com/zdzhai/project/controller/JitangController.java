package com.zdzhai.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.common.ResultUtils;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.Jitang;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.annotation.AuthCheck;
import com.zdzhai.project.common.DeleteRequest;
import com.zdzhai.project.constant.CommonConstant;
import com.zdzhai.project.model.dto.jitang.JitangAddRequest;
import com.zdzhai.project.model.dto.jitang.JitangQueryRequest;
import com.zdzhai.project.model.dto.jitang.JitangUpdateRequest;
import com.zdzhai.project.service.JitangService;
import com.zdzhai.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 帖子接口
 *
 * @author dongdong
 */
@RestController
@RequestMapping("/jitang")
@Slf4j
public class JitangController {

    @Resource
    private JitangService jitangService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param jitangAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody JitangAddRequest jitangAddRequest,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (jitangAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Jitang jitang = new Jitang();
        BeanUtils.copyProperties(jitangAddRequest, jitang);
        // 校验
        jitangService.validJitang(jitang, true);
        User loginUser = userService.getLoginUser(request,response);
        boolean result = jitangService.save(jitang);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newPostId = jitang.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request, response);
        long id = deleteRequest.getId();
        // 判断是否存在
        Jitang oldJitang = jitangService.getById(id);
        if (oldJitang == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!userService.isAdmin(request,response)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = jitangService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param jitangUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody JitangUpdateRequest jitangUpdateRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        if (jitangUpdateRequest == null || jitangUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Jitang jitang = new Jitang();
        BeanUtils.copyProperties(jitangUpdateRequest, jitang);
        // 参数校验
        jitangService.validJitang(jitang, false);
        User user = userService.getLoginUser(request, response);
        long id = jitangUpdateRequest.getId();
        // 判断是否存在
        Jitang oldjitang = jitangService.getById(id);
        if (oldjitang == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!userService.isAdmin(request,response)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = jitangService.updateById(jitang);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Jitang> getPostById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Jitang jitang = jitangService.getById(id);
        return ResultUtils.success(jitang);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param jitangQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<Jitang>> listPost(JitangQueryRequest jitangQueryRequest) {
        Jitang jitangQuery = new Jitang();
        if (jitangQueryRequest != null) {
            BeanUtils.copyProperties(jitangQueryRequest, jitangQuery);
        }
        QueryWrapper<Jitang> queryWrapper = new QueryWrapper<>(jitangQuery);
        List<Jitang> postList = jitangService.list(queryWrapper);
        return ResultUtils.success(postList);
    }

    /**
     * 分页获取列表
     *
     * @param jitangQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Jitang>> listPostByPage(JitangQueryRequest jitangQueryRequest, HttpServletRequest request) {
        if (jitangQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Jitang jitangQuery = new Jitang();
        BeanUtils.copyProperties(jitangQueryRequest, jitangQuery);
        long current = jitangQueryRequest.getCurrent();
        long size = jitangQueryRequest.getPageSize();
        String sortField = jitangQueryRequest.getSortField();
        String sortOrder = jitangQueryRequest.getSortOrder();
        String content = jitangQuery.getSentence();
        // content 需支持模糊搜索
        jitangQuery.setSentence(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Jitang> queryWrapper = new QueryWrapper<>(jitangQuery);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<Jitang> postPage = jitangService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(postPage);
    }

    // endregion

}
