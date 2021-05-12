package me.zsgoer.payment.common;

import lombok.*;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class SendStringDto {
    private String dataLength = "446";
    private String dataGubun ="";
    private String manageNo="";
    private String cardNo="";
    private int installmentMonth;
    private String expirationDate="";
    private String cvc="";
    private String amount="";
    private int vat;
    private String orgManageNo="";
    private String encCardInfo="";
    private String filler="";
    public String toGenerateSendString() {
        // 숫자    ___3
        // 숫자(0) 0003          0
        // 숫자L   3___          -
        // 문자    HOMEWORK__    -
        StringBuilder result = new StringBuilder();
        result.append(String.format("%4s",dataLength))
                .append(String.format("%-10s",dataGubun))
                .append(String.format("%-20s",manageNo))
                .append(String.format("%-20s",cardNo))
                .append(String.format("%02d",installmentMonth))
                .append(String.format("%-4s",expirationDate))
                .append(String.format("%-3s",cvc))
                .append(String.format("%10s",amount))
                .append(String.format("%010d",vat))
                .append(String.format("%20s",orgManageNo))
                .append(String.format("%-300s",encCardInfo))
                .append(String.format("%-47s",filler))
                ;
        System.out.println("["+result.toString()+"]");
        return result.toString();
    }
}
