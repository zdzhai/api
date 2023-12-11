package com.zdzhai.apicommon.model.entity.thirdparty;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dongdong
 */
@Data
public class Oauth2LoginTo implements Serializable {

    private String third_party_name;

    private String access_token;

    private String token_type;

    private int expires_in;

    private String refresh_token;

    private String scope;

    private int created_at;
}
