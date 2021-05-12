package me.zsgoer.payment.paymentrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zsgoer.payment.common.RestDocsConfiguration;
import me.zsgoer.payment.common.TestDescription;
import me.zsgoer.payment.paymentcancel.PaymentCancelDto;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class PaymentCancelControllerTests {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;




    @Test
    @TestDescription("정상적으로 결제취소요청하는 테스트")
    public void paymentCancel() throws Exception {
        PaymentRequestDto dto = generatePaymentRequest("1234567890","1223","323",0,10000);


        ResultActions manageNo = mockMvc.perform(post("/api/paymentrequest/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- 등록 확인.
                .andExpect(jsonPath("manageNo").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

        //when & then
        this.mockMvc.perform(get("/api/paymentsearch/{id}","00000000000000000001")

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("manageNo").exists())
                .andExpect(jsonPath("cardNo").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-payment",
                        links(
                                linkWithRel("self").description("self 링크입니다."),
                                linkWithRel("profile").description("API가 기술된 문서 링크입니다.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content_type header")
                        ),
                        responseFields(
                                fieldWithPath("manageNo").description("결제 관련된 관리번호입니다."),
                                fieldWithPath("cardNo").description("앞 6자리 뒤 3자리 제외한 나머지를 마스킹 처리한 카드번호입니다."),
                                fieldWithPath("expirationDate").description("유효기간 입니다."),
                                fieldWithPath("cvc").description("CVC 정보입니다."),
                                fieldWithPath("recentRequest").description("해당 관리번호의 최근 요청상태입니다. [PAYMENT:결제, CANCEL:취소]"),
                                fieldWithPath("recentAmount").description("최근요청된 결제 혹은 취소 금액입니다."),
                                fieldWithPath("recentVat").description("최근 요청 된 부가가치세 금액입니다."),
                                fieldWithPath("currentStatus").description("현재 해당 관리번호에 해당되는 결제진행 상태입니다.[PAYMENT:결제, CANCEL:취소, PARCIAL_CANCEL:부분취소]"),
                                fieldWithPath("remainAmount").description("현제 결제 상태인 금액입니다."),
                                fieldWithPath("remainVat").description("현재 결제 상태인 부가가치세입니다."),
                                fieldWithPath("_links.self.href").description("해당 API를 호출하는 URL입니다."),
                                fieldWithPath("_links.profile.href").description("해당 등록된 정보를 조회할 수 있는 링크입니다.")
                        )
                ))
        ;

        PaymentCancelDto paymentCancelDto = generatePaymentCancel("00000000000000000001",10000);
        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(paymentCancelDto))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- 취소 등록 확인.
                .andExpect(jsonPath("manageNo").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("manageNo").value(Matchers.not("00000000000000000000")))
                .andExpect(jsonPath("sendingInfo").exists())
                .andDo(document("create-payment-cancel",
                        links(
                                linkWithRel("self").description("self 링크입니다."),
                                linkWithRel("get-payment").description("해당 등록된 정보를 조회할 수 있는 링크입니다."),
                                linkWithRel("profile").description("API가 기술된 문서 링크입니다.")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content_type header")
                        ),
                        requestFields(
                                fieldWithPath("manageNo").description("[필수]관리번호 입니다."),
                                fieldWithPath("amount").description("[필수]결제취소금액 (기 결제잔여금액을 넘을 수 없음. 동일할 경우 전체취소. 적을 경우 부분취소.)"),
                                fieldWithPath("vat").description("[선택]부가가치세입니다.(기 결제부가가치세를 넘을 수 없음. )")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content_type header")
                        ),
                        responseFields(
                                fieldWithPath("manageNo").description("결제 관련된 관리번호입니다."),
                                fieldWithPath("sendingInfo").description("카드사로 보내는 String 전문입니다."),
                                fieldWithPath("_links.self.href").description("해당 API를 호출하는 URL입니다."),
                                fieldWithPath("_links.get-payment.href").description("등록된 관리번호를 조회하는 URL입니다."),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ));
        ;

    }

    private PaymentRequestDto generatePaymentRequest (String cardNo, String expirationDate, String cvc, int installmentMonth,int amount) {
        PaymentRequestDto dto = PaymentRequestDto.builder()
                .cardNo(cardNo)
                .expirationDate(expirationDate)
                .cvc(cvc)
                .installmentMonth(installmentMonth)
                .amount(amount)
                .build();
        return dto;
    }
    private PaymentRequestDto generatePaymentRequest (String cardNo, String expirationDate, String cvc, int installmentMonth,int amount, String vat) {
        PaymentRequestDto dto = PaymentRequestDto.builder()
                .cardNo(cardNo)
                .expirationDate(expirationDate)
                .cvc(cvc)
                .installmentMonth(installmentMonth)
                .amount(amount)
                .vat(vat)
                .build();
        return dto;
    }
    private PaymentCancelDto generatePaymentCancel(String manageNo,int amount) {
        PaymentCancelDto dto = PaymentCancelDto.builder()
                .manageNo(manageNo)
                .amount(amount)
                .build();
        return dto;
    }

    private PaymentCancelDto generatePaymentCancel(String manageNo,int amount, String vat) {
        PaymentCancelDto dto = PaymentCancelDto.builder()
                .manageNo(manageNo)
                .amount(amount)
                .vat(vat)
                .build();
        return dto;
    }


}
