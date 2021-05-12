package me.zsgoer.payment.paymentcancel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelDto {
    @NotBlank
    @Pattern(regexp = "^[0-9]{20}$")
    private String manageNo;
    @Min(100)
    @Max(1000000000)
    private int amount; // 금액

    private String vat; //부가가치세 -- optional.
}
