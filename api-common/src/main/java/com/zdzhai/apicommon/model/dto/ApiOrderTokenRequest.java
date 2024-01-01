package com.zdzhai.apicommon.model.dto;

import lombok.Data;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * @author dongdong
 * @Date 2023/12/30 21:23
 */
@Data
public class ApiOrderTokenRequest implements Serializable {

    Cookie[] requestCookie;
}
