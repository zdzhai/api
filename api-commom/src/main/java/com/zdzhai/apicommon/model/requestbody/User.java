package com.zdzhai.apicommon.model.requestbody;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * getNameByJson请求封装类
 * @TableName user
 */
@NoArgsConstructor
@Data
public class User implements Serializable {
    /**
     * 用户昵称
     */
    private String username;

    public User(String username) {
        this.username = username;
    }
}