package com.zdzhai.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.order.model.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 62618
* @description 针对表【seckill_voucher(秒杀优惠券表，与优惠券是一对一关系)】的数据库操作Mapper
* @createDate 2024-01-02 16:03:06
* @Entity com.zdzhai.order.model.entity.SeckillVoucher
*/
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

}




