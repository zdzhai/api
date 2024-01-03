package com.zdzhai.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdzhai.apicommon.common.BaseResponse;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.constant.CommonConstant;
import com.zdzhai.apicommon.exception.BusinessException;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.utils.ResultUtils;
import com.zdzhai.order.constant.VouvherConstant;
import com.zdzhai.order.mapper.VoucherMapper;
import com.zdzhai.order.model.dto.voucher.VoucherDTO;
import com.zdzhai.order.model.dto.voucher.VoucherQueryDTO;
import com.zdzhai.order.model.entity.SeckillVoucher;
import com.zdzhai.order.model.entity.Voucher;
import com.zdzhai.order.model.vo.VoucherVO;
import com.zdzhai.order.service.SeckillVoucherService;
import com.zdzhai.order.service.VoucherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author 62618
* @description 针对表【voucher】的数据库操作Service实现
* @createDate 2024-01-02 15:56:01
*/
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher>
    implements VoucherService{

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VoucherMapper voucherMapper;

    /**
     * 查询店铺的优惠券
     * @param interfaceId
     * @return
     */
    @Override
    public Page<VoucherVO> queryVoucherOfInterface(Long interfaceId) {
        //前端筛选即可
        long current = 1;
        // 限制爬虫
        long size = 20;
        return getBaseMapper().queryVoucherOfInterface(new Page<>(current, size), interfaceId);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void addSeckillVoucher(VoucherDTO voucherDTO) {
        // 保存优惠券
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherDTO, voucher);
        this.save(voucher);
        // 保存秒杀的优惠券信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucherDTO.getId());
        seckillVoucher.setStock(voucherDTO.getStock());
        seckillVoucher.setBeginTime(voucherDTO.getBeginTime());
        seckillVoucher.setEndTime(voucherDTO.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        //保存到redis
        stringRedisTemplate.opsForValue().set(VouvherConstant.SECKILL_STOCK_KEY + voucher.getId(),
                voucherDTO.getStock().toString());
    }

    @Override
    public Page<VoucherVO> listVouchers(VoucherQueryDTO voucherQueryDTO) {
        if (voucherQueryDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Voucher voucherQuery = new Voucher();
        BeanUtils.copyProperties(voucherQueryDTO, voucherQuery);
        long current = voucherQueryDTO.getCurrent();
        long size = voucherQueryDTO.getPageSize();
        String sortField = voucherQueryDTO.getSortField();
        String sortOrder = voucherQueryDTO.getSortOrder();
        String title = voucherQuery.getTitle();
        // content 需支持模糊搜索
        voucherQuery.setTitle(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Voucher> queryWrapper = new QueryWrapper<>(voucherQuery);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        Page<Voucher> voucherPage = this.page(new Page<>(current, size), queryWrapper);

        List<VoucherVO> voucherVOList = new ArrayList<>();
        voucherPage.getRecords().forEach(voucher -> {
            VoucherVO voucherVO = new VoucherVO();
            BeanUtils.copyProperties(voucher, voucherVO);
            //秒杀券去查自己的信息得到接口名称，库存，生效和失效时间
            String description = voucherMapper.queryInterfaceInfoByVoucherId(voucher.getInterfaceId());
            voucherVO.setDescription(description);
            if (voucher.getType() == 1) {
                 SeckillVoucher seckillVoucher = voucherMapper.queryStartEndTimeByVoucherId(voucher.getId());
                 if (seckillVoucher != null) {
                     voucherVO.setStock(seckillVoucher.getStock());
                     voucherVO.setBeginTime(seckillVoucher.getBeginTime());
                     voucherVO.setEndTime(seckillVoucher.getEndTime());
                 }
            }
            voucherVOList.add(voucherVO);
        });
        Page<VoucherVO> voucherVOPage = new Page<>(voucherPage.getCurrent(), voucherPage.getSize(), voucherPage.getTotal());
        voucherVOPage.setRecords(voucherVOList);
        return voucherVOPage;
    }

    @Override
    public BaseResponse<Boolean> onlineVoucherById(Long voucherId) {
        return ResultUtils.success(voucherMapper.onlineVoucherById(voucherId));
    }

    @Override
    public BaseResponse<Boolean> offlineVoucherById(Long voucherId) {
        return ResultUtils.success(voucherMapper.offlineVoucherById(voucherId));
    }
}




