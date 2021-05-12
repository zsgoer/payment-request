package me.zsgoer.payment.paymentcancel;

import me.zsgoer.payment.manageno.ManageNo;
import me.zsgoer.payment.manageno.RecentRequestType;
import me.zsgoer.payment.paymentcancel.PaymentCancel;
import me.zsgoer.payment.paymentcancel.PaymentCancelDto;
import me.zsgoer.payment.paymentrequest.PaymentRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PaymentCancelValidator {
    public void validate(ManageNo currentPayment, PaymentCancelDto paymentCancelDto, Errors errors) {
        if (!errors.hasErrors()) {

            int currentAmount = currentPayment.getAmount();
            int currentVat    = currentPayment.getVat();

            int requestAmount = paymentCancelDto.getAmount();

            if (paymentCancelDto.getVat()==null) {
                int requestVat = Math.round(requestAmount/11); // 부가가치세 자동 계산.

                if (currentAmount == requestAmount) {
                    requestVat = currentVat;
                }

                if (currentVat < requestVat) {//
                    errors.rejectValue("vat", "wrongValue", "부가가치세취소금액이 결제상태인 부가가치세 금액을 초과할 수 없습니다.");
                }

                paymentCancelDto.setVat(String.valueOf(requestVat));
            } else {
                //1. vat 숫자인지 검증.
                boolean isNumeric = paymentCancelDto.getVat().matches("[+-]?\\d*(\\.\\d+)?");
                if(isNumeric) {
                    int requestVat = Integer.valueOf(paymentCancelDto.getVat());
                    if (currentVat < requestVat) {//
                        errors.rejectValue("vat", "wrongValue", "부가가치세취소금액이 결제상태인 부가가치세를 초과할 수 없습니다.");
                    }
                }else {
                    errors.rejectValue("vat","wrongValue","부가가치세가 문자열입니다. 숫자만 가능합니다.");
                }
            }

            if (currentAmount<requestAmount) { // 현재 남아있는 결제금액이 현재보다 큰 경우 에러 리턴.
                errors.rejectValue("amount","wrongValue","결제취소금액이 결제상태인 금액보다 큽니다.");
            } else if (currentAmount == requestAmount )  {
                currentPayment.setRecentRequest(RecentRequestType.CANCEL.name());
                int requestVat = Integer.valueOf(paymentCancelDto.getVat()); // 상단에서 Vat 가격 집어 넣음.
                if (currentVat != requestVat) {
                    errors.rejectValue("vat","wrongValue","결제취소금액과 결제 상태인 금액이 동일하나, 결제상대인 부가가치세가 남아있어 취소가 불가능합니다.");
                }

            } else {
                currentPayment.setRecentRequest(RecentRequestType.PARCIAL_CANCEL.name());
            }

        }
    }
}
