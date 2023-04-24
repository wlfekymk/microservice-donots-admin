package com.kyobo.platform.donots.common.exception;

public class PasswordTenCountNotMatchException extends BusinessException {
    public PasswordTenCountNotMatchException() {
        super(ErrorCode.PASSWORD_TEN_COUNT_NOT_MATCH.status, ErrorCode.PASSWORD_TEN_COUNT_NOT_MATCH.message);
    }

    public PasswordTenCountNotMatchException(String caseSpecificMessage) {
        super(ErrorCode.PASSWORD_TEN_COUNT_NOT_MATCH.status, caseSpecificMessage);
    }
}
