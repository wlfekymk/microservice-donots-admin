package com.kyobo.platform.donots.common.exception;

public class PasswordFiveCountNotMatchException extends BusinessException {
    public PasswordFiveCountNotMatchException() {
        super(ErrorCode.PASSWORD_FIVE_COUNT_NOT_MATCH.status, ErrorCode.PASSWORD_FIVE_COUNT_NOT_MATCH.message);
    }

    public PasswordFiveCountNotMatchException(String caseSpecificMessage) {
        super(ErrorCode.PASSWORD_FIVE_COUNT_NOT_MATCH.status, caseSpecificMessage);
    }
}
