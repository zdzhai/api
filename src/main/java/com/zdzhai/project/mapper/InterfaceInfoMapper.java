package com.zdzhai.project.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.project.model.vo.EchartsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 62618
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-03-17 21:30:28
* @Entity com.zdzhai.project.model.entity.InterfaceInfo
*/
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {

    List<EchartsVO> getInterfaceList(@Param("dateList") List<String> dateList);
}




