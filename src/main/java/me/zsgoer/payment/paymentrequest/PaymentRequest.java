package me.zsgoer.payment.paymentrequest;

import lombok.*;
import me.zsgoer.payment.common.KISA_SEED_CBC;
import me.zsgoer.payment.manageno.RecentRequestType;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Base64.Decoder;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "manageNo")
@Entity
public class PaymentRequest {
    @Id
    private String manageNo;

    private String encCardInfo; // 카드번호|유효기간|CVC
    private int installmentMonth; //할부개월
    private int amount; // 금액
    private int vat; //부가가치세

    @Column(length = 450)
    private String sendingInfo; //카드사 송부 정보.

    @Transient
    private String cardNo;
    @Transient
    private String expirationDate;
    @Transient
    private String cvc;
    @Transient
    private String recentRequest = RecentRequestType.PAYMENT.name();

    // 카드 번호를 조합하여 암호화하는 메소드
    public void updateEncCardInfo() {
        //update encCardInfo
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.cardNo).append(KISA_SEED_CBC.DELEMETER)
                .append(this.expirationDate).append(KISA_SEED_CBC.DELEMETER)
                .append(this.cvc);

        String strEncCardInfo = stringBuilder.toString();
        byte[] encryptedMessage = KISA_SEED_CBC.SEED_CBC_Encrypt(KISA_SEED_CBC.PBSZ_USER_KEY, KISA_SEED_CBC.PBSZ_IV, strEncCardInfo.getBytes(StandardCharsets.UTF_8), 0, strEncCardInfo.getBytes(StandardCharsets.UTF_8).length);

        Encoder encoder = Base64.getEncoder();
        this.encCardInfo =  new String(encoder.encode(encryptedMessage), StandardCharsets.UTF_8);
    }

    public void updateDecCardInfo() {
        //update DecCardInfo

        Decoder decoder = Base64.getDecoder();
        byte[] endCardInfoByteArr = decoder.decode(this.encCardInfo);
        byte[] decryptedMessage = KISA_SEED_CBC.SEED_CBC_Decrypt(KISA_SEED_CBC.PBSZ_USER_KEY, KISA_SEED_CBC.PBSZ_IV, endCardInfoByteArr, 0, endCardInfoByteArr.length);
        String strDecCardInfo = new String(decryptedMessage, StandardCharsets.UTF_8);
        String[] arrCardInfo = strDecCardInfo.split("\\|");
        this.cardNo = arrCardInfo[0];
        this.expirationDate = arrCardInfo[1];
        this.cvc = arrCardInfo[2];
    }


}
