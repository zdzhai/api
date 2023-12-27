package com.zdzhai.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.apicommon.model.entity.ApiOrder;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 62618
* @description 针对表【api_order】的数据库操作Mapper
* @createDate 2023-12-17 16:48:17
* @Entity com.zdzhai.project.model.entity.ApiOrder
*/
@Mapper
public interface ApiOrderMapper extends BaseMapper<ApiOrder> {

    /**
     * 根据订单号修改订单状态
     * @param orderSn
     * @param status
     * @return
     */
    int updateApiOrderStatusByOrderSn(String orderSn, int status);

    /**
     * 根据订单号查找订单信息
     * @param orderSn
     * @return
     */
    ApiOrder getApiOrderByOrderSn(String orderSn);
}




