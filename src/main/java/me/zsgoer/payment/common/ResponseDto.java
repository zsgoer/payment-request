package me.zsgoer.payment.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto {
    private String manageNo;
    private String sendingInfo;
}
