package com.zdzhai.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.order.model.entity.SeckillVoucher;
import com.zdzhai.order.model.entity.Voucher;
import com.zdzhai.order.model.vo.VoucherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 62618
* @description 针对表【voucher】的数据库操作Mapper
* @createDate 2024-01-02 15:56:01
* @Entity com.zdzhai.order.model.entity.Voucher
*/
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 查询店铺的优惠券
     * @param interfaceId
     * @return
     */
    Page<VoucherVO> queryVoucherOfInterface(Page<Object> objectPage,
                                            @Param("interfaceInfoId") Long interfaceId);

    /**
     * 上架
     * @param voucherId
     * @return
     */
    boolean onlineVoucherById(Long voucherId);

    /**
     * 下架
     * @param voucherId
     * @return
     */
    boolean offlineVoucherById(Long voucherId);

    String queryInterfaceInfoByVoucherId(Long interfaceId);

    SeckillVoucher queryStartEndTimeByVoucherId(Long id);
}




