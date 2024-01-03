package com.zdzhai.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.order.model.dto.voucher.VoucherDTO;
import com.zdzhai.order.model.dto.voucher.VoucherQueryDTO;
import com.zdzhai.order.model.entity.Voucher;
import com.zdzhai.order.model.vo.VoucherVO;

/**
* @author 62618
* @description 针对表【voucher】的数据库操作Service
* @createDate 2024-01-02 15:56:01
*/
public interface VoucherService extends IService<Voucher> {
    /**
     * 查询店铺的优惠券
     * @param interfaceId
     * @return
     */
    Page<VoucherVO> queryVoucherOfInterface(Long interfaceId);

    /**
     * 给店铺添加优惠券
     * @param voucherDTO
     */
    void addSeckillVoucher(VoucherDTO voucherDTO);

    /**
     * 查询所有优惠券
     * @return
     */
    Page<VoucherVO> listVouchers(VoucherQueryDTO voucherQueryDTO);

    /**
     * 上架优惠券
     * @param voucherId
     * @return
     */
    BaseResponse<Boolean> onlineVoucherById(Long voucherId);

    /**
     * 下架优惠券
     * @param voucherId
     * @return
     */
    BaseResponse<Boolean> offlineVoucherById(Long voucherId);
}
