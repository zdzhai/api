package com.zdzhai.project.model.vo;

import com.zdzhai.apicommon.model.entity.User;
import lombok.Data;

/**
 * @author dongdong
 * @Date 2023/4/15 10:32
 */
@Data
public class LoginUserVO extends User {
    public String token;
}
