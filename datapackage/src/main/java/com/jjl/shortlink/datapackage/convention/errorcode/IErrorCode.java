package com.jjl.shortlink.datapackage.convention.errorcode;

/**
 * 平台错误码
 */
public interface IErrorCode {

    /**
     * 错误码
     */
    String code();

    /**
     * 错误信息
     */
    String message();
}