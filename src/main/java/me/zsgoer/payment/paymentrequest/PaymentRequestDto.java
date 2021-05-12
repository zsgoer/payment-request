package me.zsgoer.payment.paymentrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentRequestDto {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,16}$")
    private String cardNo;
    @NotBlank
    @Pattern(regexp = "^[0-9]{4}$")
    private String expirationDate;
    @NotBlank
    @Pattern(regexp = "^[0-9]{3}$")
    private String cvc;
    @Min(0)
    @Max(12)
    private int installmentMonth; //할부개월
    @Min(100)
    @Max(1000000000)
    private int amount; // 금액



    private String vat; //부가가치세 -- optional.


}
