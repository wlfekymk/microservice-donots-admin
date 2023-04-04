package com.kyobo.platform.donots.common.exception;

public class InvalidSessionException extends BusinessException {

    public InvalidSessionException() {
        super(ErrorCode.INVALID_SESSION.status, ErrorCode.INVALID_SESSION.message);
    }

    public InvalidSessionException(String caseSpecificMessage) {
        super(ErrorCode.INVALID_SESSION.status, caseSpecificMessage);
    }
}