package com.zdzhai.order.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.order.model.dto.voucher.VoucherDTO;
import com.zdzhai.order.model.dto.voucher.VoucherQueryDTO;
import com.zdzhai.order.model.dto.voucher.VoucherUpdateDTO;
import com.zdzhai.order.model.entity.SeckillVoucher;
import com.zdzhai.order.model.entity.Voucher;
import com.zdzhai.order.model.vo.VoucherVO;
import com.zdzhai.order.service.SeckillVoucherService;
import com.zdzhai.order.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *
 * @author dongdong
 */
@RestController
@RequestMapping("/voucher")
@Slf4j
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    @Resource
    private SeckillVoucherService seckillVoucherService;

    /**
     * 新增优惠券
     * @param voucherDTO 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("add")
    public BaseResponse<Long> addVoucher(@RequestBody VoucherDTO voucherDTO) {
        voucherService.addSeckillVoucher(voucherDTO);
        return ResultUtils.success(voucherDTO.getId());
    }

    /**
     * 查询店铺的优惠券列表
     * @param interfaceId 接口id
     * @return 优惠券列表
     */
    @GetMapping("/listVoucher")
    public BaseResponse<Page<VoucherVO>> queryVoucherOfInterface(@PathVariable("interfaceId") Long interfaceId) {
        Page<VoucherVO> voucherVOPage = voucherService.queryVoucherOfInterface(interfaceId);
       return ResultUtils.success(voucherVOPage);
    }

    /**
     * 查询所有的优惠券列表
     * @param voucherQueryDTO
     * @return 优惠券列表
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<VoucherVO>> listVouchers(VoucherQueryDTO voucherQueryDTO) {
        Page<VoucherVO> voucherVOPage = voucherService.listVouchers(voucherQueryDTO);

        return ResultUtils.success(voucherVOPage);
    }

    /**
     * 上架优惠券
     * @param voucherId
     * @return
     */
    @PostMapping("/online")
    public BaseResponse<Boolean> onlineVoucherById(Long voucherId) {
        return voucherService.onlineVoucherById(voucherId);
    }

    /**
     * 下架优惠券
     * @param voucherId
     * @return
     */
    @PostMapping("/offline")
    public BaseResponse<Boolean> offlineVoucherById(Long voucherId) {
        return voucherService.offlineVoucherById(voucherId);
    }

    /**
     * 修改优惠券信息
     * @param voucherUpdateDTO
     * @return
     */
    @PostMapping("/update")
    @Transactional
    public BaseResponse<Boolean> updateVoucher(@RequestBody VoucherUpdateDTO voucherUpdateDTO) {
        if (voucherUpdateDTO == null || voucherUpdateDTO.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = voucherUpdateDTO.getId();
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherUpdateDTO, voucher);
        // 判断是否存在
        Voucher dbVoucher = voucherService.getById(id);
        if (dbVoucher == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = false;
        try {
            result = voucherService.updateById(voucher);
            SeckillVoucher seckillVoucher = new SeckillVoucher();
            if (voucherUpdateDTO.getType() == 1) {
                seckillVoucher.setVoucherId(id);
                BeanUtils.copyProperties(voucherUpdateDTO, seckillVoucher);
                result = seckillVoucherService.updateById(seckillVoucher);
            }
        } catch (Exception e) {
            log.error("更新优惠券信息失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(result);
    }

    /**
     * 接口名称
     * 优惠券标题
     * 秒杀券开始和截止时间
     * 使用时间全时间段，不做要求
     */

}
