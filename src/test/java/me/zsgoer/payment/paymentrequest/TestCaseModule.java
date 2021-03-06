package me.zsgoer.payment.paymentrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.zsgoer.payment.common.RestDocsConfiguration;
import me.zsgoer.payment.common.SendStringDto;
import me.zsgoer.payment.common.TestDescription;
import me.zsgoer.payment.controller.PaymentRequestController;
import me.zsgoer.payment.manageno.ManageNo;
import me.zsgoer.payment.manageno.ManageNoRepository;
import me.zsgoer.payment.manageno.RecentRequestType;
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
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class TestCaseModule {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Autowired
    ManageNoRepository manageNoRepository;

    @Test
    @TestDescription("4??? ???????????? TESTCASE 1 ")
    public void testcase1 () throws Exception {
        //Given : 11000, 1000
        PaymentRequestDto dto = generatePaymentRequest("1234567890"
                ,"1223"
                ,"323"
                ,0
                ,11000
                ,"1000");

        PaymentRequest paymentRequest = modelMapper.map(dto, PaymentRequest.class);
        paymentRequest.updateEncCardInfo();

        //???????????? ????????? ?????? SEQUENCE ??????
        ManageNo manageNo = manageNoRepository.save(new ManageNo());
        //????????????
        String strManageNo = String.format("%020d",manageNo.getId());

        //??????????????? ??????????????? SET
        paymentRequest.setManageNo(strManageNo);
        //??????String Data ??????.
        paymentRequest.setSendingInfo(strBuildStringPaymentRequestData(paymentRequest));
        //?????????????????? ??????.
        PaymentRequest newPaymentRequest = paymentRequestRepository.save(paymentRequest);

        modelMapper.map(newPaymentRequest,manageNo);
        manageNo.setRecentRequest(RecentRequestType.PAYMENT.name());
        manageNo = manageNoRepository.save(manageNo);


/*
        mockMvc.perform(post("/api/paymentrequest/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())  ;      // 201 -- ?????? ??????.
*/

        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(11000))
                .andExpect(jsonPath("remainVat").value(1000))
        ;//

        //???????????? ()
        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),1100,"100")))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ?????? ?????? ??????.
        ;
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(9900))
                .andExpect(jsonPath("remainVat").value(900))
        ;//


        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),3300)))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ?????? ?????? ??????.
        ;
        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(6600))
                .andExpect(jsonPath("remainVat").value(600))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),7000)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())        // 400 ????????? ?????? ???????????? ??????.
        ;

        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(6600))
                .andExpect(jsonPath("remainVat").value(600))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),6600,"700")))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())        // 400 ????????? ?????? ???????????? ??????. - ?????????????????? ??? ???.
        ;

        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(6600))
                .andExpect(jsonPath("remainVat").value(600))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),6600,"600")))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ?????? ?????? ??????.
        ;

        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo()) ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(0))
                .andExpect(jsonPath("remainVat").value(0))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),100,null)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())       //??????????????? ????????? ??????????????? ???????????? 400 ?????? ??????.
        ;
    }

    @Test
    @TestDescription("4??? ???????????? TESTCASE 2 ")
    public void testcase2 () throws Exception {
        //Given : 11000, 1000
        PaymentRequestDto dto = generatePaymentRequest("1234567890"
                ,"1223"
                ,"323"
                ,0
                ,20000
                ,"909");

        PaymentRequest paymentRequest = modelMapper.map(dto, PaymentRequest.class);
        paymentRequest.updateEncCardInfo();

        //???????????? ????????? ?????? SEQUENCE ??????
        ManageNo manageNo = manageNoRepository.save(new ManageNo());
        //????????????
        String strManageNo = String.format("%020d",manageNo.getId());

        //??????????????? ??????????????? SET
        paymentRequest.setManageNo(strManageNo);
        //??????String Data ??????.
        paymentRequest.setSendingInfo(strBuildStringPaymentRequestData(paymentRequest));
        //?????????????????? ??????.
        PaymentRequest newPaymentRequest = paymentRequestRepository.save(paymentRequest);

        modelMapper.map(newPaymentRequest,manageNo);
        manageNo.setRecentRequest(RecentRequestType.PAYMENT.name());
        manageNo = manageNoRepository.save(manageNo);

/*
        ResultActions manageNo = mockMvc.perform(post("/api/paymentrequest/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())  ;      // 201 -- ?????? ??????.
*/

        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(20000))
                .andExpect(jsonPath("remainVat").value(909))
        ;//

        //???????????? ()
        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000,"0")))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ?????? ?????? ??????.
        ;
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(10000))
                .andExpect(jsonPath("remainVat").value(909))
        ;//???????????? ??? ?????? ??? ??????.


        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000, "0")))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())        // 404 --
        ;
        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(10000))
                .andExpect(jsonPath("remainVat").value(909))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000,"909")))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 400 ????????? ?????? ???????????? ??????.
        ;

        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(0))
                .andExpect(jsonPath("remainVat").value(0))
        ;
    }

    @Test
    @TestDescription("4??? ???????????? TESTCASE 3 ")
    public void testcase3 () throws Exception {
        //Given : 11000, 1000
        PaymentRequestDto dto = generatePaymentRequest("1234567890"
                ,"1223"
                ,"323"
                ,0
                ,20000
                );
        dto.setVat(String.valueOf(Math.round(dto.getAmount()/11)));

        PaymentRequest paymentRequest = modelMapper.map(dto, PaymentRequest.class);
        paymentRequest.updateEncCardInfo();

        //???????????? ????????? ?????? SEQUENCE ??????
        ManageNo manageNo = manageNoRepository.save(new ManageNo());
        //????????????
        String strManageNo = String.format("%020d",manageNo.getId());

        //??????????????? ??????????????? SET
        paymentRequest.setManageNo(strManageNo);
        //??????String Data ??????.
        paymentRequest.setSendingInfo(strBuildStringPaymentRequestData(paymentRequest));
        //?????????????????? ??????.
        PaymentRequest newPaymentRequest = paymentRequestRepository.save(paymentRequest);

        modelMapper.map(newPaymentRequest,manageNo);
        manageNo.setRecentRequest(RecentRequestType.PAYMENT.name());
        manageNo = manageNoRepository.save(manageNo);

/*
        ResultActions manageNo = mockMvc.perform(post("/api/paymentrequest/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())  ;      // 201 -- ?????? ??????.
*/

        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(20000))
                .andExpect(jsonPath("remainVat").value(1818))
        ;//

        //???????????? ()
        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000,"1000")))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 201 -- ?????? ?????? ?????? ??????.
        ;
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(10000))
                .andExpect(jsonPath("remainVat").value(818))
        ;//???????????? ??? ?????? ??? ??????.


        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000, "909")))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())        // 404 --
        ;
        //when & then -- ?????????????????????.
        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(10000))
                .andExpect(jsonPath("remainVat").value(818))
        ;

        mockMvc.perform(post("/api/paymentcancel/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(generatePaymentCancel(manageNo.getManageNo(),10000)))
        )
                .andDo(print())
                .andExpect(status().isCreated())        // 400 ????????? ?????? ???????????? ??????.
        ;

        this.mockMvc.perform(get("/api/paymentsearch/{id}",manageNo.getManageNo())

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("remainAmount").value(0))
                .andExpect(jsonPath("remainVat").value(0))
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
    private PaymentCancelDto generatePaymentCancel(String manageNo, int amount) {
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
    private String strBuildStringPaymentRequestData (PaymentRequest paymentRequest) {
        SendStringDto map = modelMapper.map(paymentRequest, SendStringDto.class);
        map.setDataGubun(RecentRequestType.PAYMENT.name());

        return map.toGenerateSendString();
    }
}
