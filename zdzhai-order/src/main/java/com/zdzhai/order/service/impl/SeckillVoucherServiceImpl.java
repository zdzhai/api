package com.zdzhai.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.order.model.entity.SeckillVoucher;
import com.zdzhai.order.mapper.SeckillVoucherMapper;
import com.zdzhai.order.service.SeckillVoucherService;
import org.springframework.stereotype.Service;

/**
* @author 62618
* @description 针对表【seckill_voucher(秒杀优惠券表，与优惠券是一对一关系)】的数据库操作Service实现
* @createDate 2024-01-02 16:03:06
*/
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher>
    implements SeckillVoucherService {

}




