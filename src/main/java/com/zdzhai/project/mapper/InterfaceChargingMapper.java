package com.zdzhai.project.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdzhai.project.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.zdzhai.project.model.entity.InterfaceCharging;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.project.model.vo.InterfaceChargingVO;
import org.apache.ibatis.annotations.Param;

/**
* @author dongdong
* @description 针对表【interface_charging】的数据库操作Mapper
* @createDate 2023-12-11 12:09:00
* @Entity com.zdzhai.project.model.entity.InterfaceCharging
*/
public interface InterfaceChargingMapper extends BaseMapper<InterfaceCharging> {


    Page<InterfaceChargingVO> selectOnlinePage(Page<Object> objectPage);
}




