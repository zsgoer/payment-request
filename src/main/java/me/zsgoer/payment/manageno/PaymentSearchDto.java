package me.zsgoer.payment.manageno;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchDto {
    private String manageNo;
    private String cardNo;
    private String expirationDate;
    private String cvc;
    private String recentRequest;
    private int    recentAmount;
    private int    recentVat;
    private String currentStatus;
    private int    remainAmount;
    private int    remainVat;
}
