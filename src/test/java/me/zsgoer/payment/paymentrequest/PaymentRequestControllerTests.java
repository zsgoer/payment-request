package me.zsgoer.payment.paymentrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zsgoer.payment.common.BaseControllerTest;
import me.zsgoer.payment.common.RestDocsConfiguration;
import me.zsgoer.payment.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Ignore;
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

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class PaymentRequestControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Test
    @TestDescription("??????????????? ??????????????? ???????????? ?????????")
    public void paymentRequest() throws Exception {
        PaymentRequestDto dto = PaymentRequestDto.builder()
                .cardNo("1234567890")
                .expirationDate("1223")
                .cvc("323")
                .installmentMonth(0)
                .amount(10000)
                .build();

        mockMvc.perform(post("/api/paymentrequest/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ??????.
                .andExpect(jsonPath("manageNo").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("manageNo").value(Matchers.not("00000000000000000000")))
                .andExpect(jsonPath("sendingInfo").exists())
                .andDo(document("create-payment-request",
                        links(
                                linkWithRel("self").description("self ???????????????."),
                                linkWithRel("get-payment").description("link to query payment info"),
                                linkWithRel("profile").description("link to update an existing event")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content_type header")
                        ),
                        requestFields(
                                fieldWithPath("cardNo").description("[??????]???????????? ?????????."),
                                fieldWithPath("expirationDate").description("[??????]???????????? ?????????."),
                                fieldWithPath("cvc").description("[??????]CVC(3?????? ??????) ?????????."),
                                fieldWithPath("installmentMonth").description("[??????]??????????????? 0(?????????), 1~12"),
                                fieldWithPath("amount").description("[??????]???????????? (100?????? ~10?????????)"),
                                fieldWithPath("vat").description("[??????]????????????????????????.")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content_type header")
                        ),
                        responseFields(
                                fieldWithPath("manageNo").description("?????? ????????? ?????????????????????."),
                                fieldWithPath("sendingInfo").description("???????????? ????????? String ???????????????."),
                                fieldWithPath("_links.self.href").description("?????? API??? ???????????? URL?????????."),
                                fieldWithPath("_links.get-payment.href").description("????????? ??????????????? ???????????? URL?????????."),
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
}
