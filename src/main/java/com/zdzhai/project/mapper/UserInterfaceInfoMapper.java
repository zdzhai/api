package com.zdzhai.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.apicommon.model.entity.UserInterfaceInfo;
import com.zdzhai.project.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import com.zdzhai.project.model.vo.UserInterfaceInfoVO;
import com.zdzhai.project.model.vo.UserInterfaceLeftNumVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 62618
* @description 针对表【user_interface_info(用户调用接口信息表)】的数据库操作Mapper
* @createDate 2023-03-24 19:57:31
* @Entity com.zdzhai.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

/*    select interfaceInfoId, sum(totalNum) as num
    from user_interface_info
    group by interfaceInfoId
    order by num desc
    limit 3*/

    /**
     * 查询topN的接口信息
     * @param limit
     * @return
     */
    List<UserInterfaceInfoVO> listTopInvokeInterfaceInfo(int limit);

    /**
     * 获取当前登录用户的接口剩余调用次数
     * @param loginUserId
     * @return
     */
    List<UserInterfaceLeftNumVO> getUserInterfaceLeftNum(@Param("id") Long loginUserId);

    /**
     * 用户订购后，更新用户接口的接口剩余数量
     * @param userInterfaceInfoUpdateRequest
     * @return
     */
    int updateUserInterfaceInfoLeftNum(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest);
}




