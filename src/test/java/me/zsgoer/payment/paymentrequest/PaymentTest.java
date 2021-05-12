package me.zsgoer.payment.paymentrequest;

import junitparams.JUnitParamsRunner;
import me.zsgoer.payment.common.TestDescription;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class PaymentTest {

    @Test
    @TestDescription("builder로 객체 생성이 되는지 확인.")
    public void builder() {
        PaymentRequest payment = PaymentRequest.builder()
                .cardNo("1234567890")
                .expirationDate("0129")
                .cvc("323")
                .build();
        assertThat(payment).isNotNull();
    }

    @Test
    @TestDescription("암호화가 정상동작하는지 확인")
    public void check_UpdateEncCardInfo() {
        PaymentRequest payment = PaymentRequest.builder()
                .cardNo("1234567890")
                .expirationDate("0129")
                .cvc("323")
                .build();

        payment.updateEncCardInfo();

        assertThat(payment.getEncCardInfo()).isNotNull();
    }

    @Test
    @TestDescription("암복호화가 정상동작하는 지 확인.")
    public void check_UpdateDecCardInfo() {

        String cardNo = "1234567890";
        String expirationDate = "0129";
        String cvc = "323";
        PaymentRequest payment = PaymentRequest.builder()
                .cardNo(cardNo)
                .expirationDate(expirationDate)
                .cvc(cvc)
                .build();

        payment.updateEncCardInfo();

        PaymentRequest newPayment = PaymentRequest.builder()
                .encCardInfo(payment.getEncCardInfo())
                .build();

        newPayment.updateDecCardInfo();



        assertThat(newPayment.getCardNo()).isEqualTo(cardNo);
        assertThat(newPayment.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(newPayment.getCvc()).isEqualTo(cvc);
    }

    @Test
    @TestDescription("부가가치세가 Null일 경우")
    public void check_VatIsNull () {
        System.out.println((Integer)Math.round(10000/11));
    }


}
