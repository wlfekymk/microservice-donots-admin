package com.kyobo.platform.donots.common.exception;

public class InsufficientPermissionException extends BusinessException {

    public InsufficientPermissionException() {
        super(ErrorCode.INSUFFICIENT_PERMISSION.status, ErrorCode.INSUFFICIENT_PERMISSION.message);
    }

    public InsufficientPermissionException(String caseSpecificMessage) {
        super(ErrorCode.INSUFFICIENT_PERMISSION.status, caseSpecificMessage);
    }
}