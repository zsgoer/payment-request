package me.zsgoer.payment.paymentrequest;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PaymentRequestValidator {
    public void validate(PaymentRequestDto paymentRequestDto, Errors errors) {
        if (!errors.hasErrors()) {
            int iAmount = paymentRequestDto.getAmount();
            if (paymentRequestDto.getVat()==null) { //부가가치세가 Null인경우 자동계산.
                paymentRequestDto.setVat(String.valueOf(Math.round(iAmount/11)));
            } else {
                //1. vat 숫자인지 검증.
                boolean isNumeric = paymentRequestDto.getVat().matches("[+-]?\\d*(\\.\\d+)?");
                if (isNumeric) {
                    int iVat = Integer.valueOf(paymentRequestDto.getVat());
                    if (iAmount < iVat) { //부가가치세가 가격보다 클 경우 에러.
                        errors.rejectValue("vat","wrongValue","부가가치세가 결제금액을 초과할 수 없습니다.");
                    }
                } else {
                    errors.rejectValue("vat","wrongValue","부가가치세가 문자열입니다. 숫자만 가능합니다.");
                }

            }
        }
    }
}
