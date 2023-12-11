package com.zdzhai.project.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.project.model.vo.EchartsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 62618
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-04-18 21:26:04
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    String selectMobileNum(@Param("mobile") String mobile);

    List<EchartsVO> getUserList(@Param("dateList") List<String> dateList);
}




