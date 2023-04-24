package com.zdzhai.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.common.ResultUtils;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.Tuwei;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.annotation.AuthCheck;

import com.zdzhai.project.common.DeleteRequest;


import com.zdzhai.project.constant.CommonConstant;

import com.zdzhai.project.model.dto.tuwei.TuweiAddRequest;
import com.zdzhai.project.model.dto.tuwei.TuweiQueryRequest;
import com.zdzhai.project.model.dto.tuwei.TuweiUpdateRequest;
import com.zdzhai.project.service.TuweiService;
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
@RequestMapping("/tuwei")
@Slf4j
public class TuweiController {

    @Resource
    private TuweiService tuweiService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param tuweiAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody TuweiAddRequest tuweiAddRequest,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (tuweiAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Tuwei tuwei = new Tuwei();
        BeanUtils.copyProperties(tuweiAddRequest, tuwei);
        // 校验
        tuweiService.validTuwei(tuwei, true);
        User loginUser = userService.getLoginUser(request,response);
        boolean result = tuweiService.save(tuwei);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newPostId = tuwei.getId();
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
        Tuwei oldTuwei = tuweiService.getById(id);
        if (oldTuwei == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!userService.isAdmin(request,response)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = tuweiService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param tuweiUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody TuweiUpdateRequest tuweiUpdateRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        if (tuweiUpdateRequest == null || tuweiUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Tuwei tuwei = new Tuwei();
        BeanUtils.copyProperties(tuweiUpdateRequest, tuwei);
        // 参数校验
        tuweiService.validTuwei(tuwei, false);
        User user = userService.getLoginUser(request, response);
        long id = tuweiUpdateRequest.getId();
        // 判断是否存在
        Tuwei oldtuwei = tuweiService.getById(id);
        if (oldtuwei == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!userService.isAdmin(request,response)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = tuweiService.updateById(tuwei);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Tuwei> getPostById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Tuwei tuwei = tuweiService.getById(id);
        return ResultUtils.success(tuwei);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param tuweiQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<Tuwei>> listPost(TuweiQueryRequest tuweiQueryRequest) {
        Tuwei tuweiQuery = new Tuwei();
        if (tuweiQueryRequest != null) {
            BeanUtils.copyProperties(tuweiQueryRequest, tuweiQuery);
        }
        QueryWrapper<Tuwei> queryWrapper = new QueryWrapper<>(tuweiQuery);
        List<Tuwei> postList = tuweiService.list(queryWrapper);
        return ResultUtils.success(postList);
    }

    /**
     * 分页获取列表
     *
     * @param tuweiQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Tuwei>> listPostByPage(TuweiQueryRequest tuweiQueryRequest, HttpServletRequest request) {
        if (tuweiQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Tuwei tuweiQuery = new Tuwei();
        BeanUtils.copyProperties(tuweiQueryRequest, tuweiQuery);
        long current = tuweiQueryRequest.getCurrent();
        long size = tuweiQueryRequest.getPageSize();
        String sortField = tuweiQueryRequest.getSortField();
        String sortOrder = tuweiQueryRequest.getSortOrder();
        String content = tuweiQuery.getSentence();
        // content 需支持模糊搜索
        tuweiQuery.setSentence(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Tuwei> queryWrapper = new QueryWrapper<>(tuweiQuery);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<Tuwei> postPage = tuweiService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(postPage);
    }

    // endregion

}
