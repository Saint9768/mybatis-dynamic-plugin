package com.saint.dynamic.model;

import lombok.Data;

@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 年龄
     */
    private int age;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 操作人
     */
    private Long operatorId;
}
