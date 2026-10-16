package com.example.grzy.common.exception;

import com.example.grzy.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author sodasmell
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
}
