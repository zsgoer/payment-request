package me.zsgoer.payment.controller;

import me.zsgoer.payment.manageno.ManageNo;
import me.zsgoer.payment.manageno.ManageNoRepository;
import me.zsgoer.payment.manageno.PaymentSearchDto;
import me.zsgoer.payment.manageno.RecentRequestType;
import me.zsgoer.payment.paymentcancel.PaymentCancel;
import me.zsgoer.payment.paymentcancel.PaymentCancelRepository;
import me.zsgoer.payment.paymentrequest.PaymentRequest;
import me.zsgoer.payment.paymentrequest.PaymentRequestRepository;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value="/api/paymentsearch", produces= MediaTypes.HAL_JSON_VALUE)
public class PaymentSearchController {
    private static final String CARD_NO_MASKING_PATTERN ="^(\\d{6})?++(\\d{3})";
    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentCancelRepository paymentCancelRepository;
    private final ManageNoRepository manageNoRepository;
    private final ModelMapper modelMapper ;

    public PaymentSearchController(PaymentRequestRepository paymentRequestRepository,
                                   PaymentCancelRepository paymentCancelRepository,
                                   ManageNoRepository manageNoRepository,
                                   ModelMapper modelMapper){
        this.paymentRequestRepository = paymentRequestRepository;
        this.paymentCancelRepository = paymentCancelRepository;
        this.manageNoRepository = manageNoRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{manageNo}")
    public ResponseEntity getEvent(@PathVariable String manageNo) {
        ManageNo targetPayment = this.manageNoRepository.findByManageNo(manageNo);
        if (targetPayment.getManageNo().isEmpty()) {
            return notFound();
        }
        targetPayment.updateDecCardInfo();
        PaymentSearchDto paymentSearchDto = modelMapper.map(targetPayment, PaymentSearchDto.class);
        //카드번호 마스킹처리.
        paymentSearchDto.setCardNo(maskingCardNo(paymentSearchDto.getCardNo()));
        paymentSearchDto.setCurrentStatus(paymentSearchDto.getRecentRequest());
        paymentSearchDto.setRemainAmount(targetPayment.getAmount());
        paymentSearchDto.setRemainVat(targetPayment.getVat());

        //현재 상태를 최근상태로 업데이트 한다. (스팩 상 결제/취소로만 되어있다.)

        if (targetPayment.getRecentRequest().equals(RecentRequestType.PAYMENT.name())) {
            //최근
            Optional<PaymentRequest> byId = this.paymentRequestRepository.findById(manageNo);
            PaymentRequest paymentRequest = byId.get();
            paymentSearchDto.setRecentAmount(paymentRequest.getAmount());
            paymentSearchDto.setRecentVat(paymentRequest.getVat());
            paymentSearchDto.setRecentRequest(RecentRequestType.PAYMENT.name());
        } else {
            List<PaymentCancel> recentCancelList = this.paymentCancelRepository.findByManageNoOrderByCancelIdDesc(manageNo);
            PaymentCancel recentCancel = recentCancelList.get(0);
            paymentSearchDto.setRecentAmount(recentCancel.getAmount());
            paymentSearchDto.setRecentVat(recentCancel.getVat());
            paymentSearchDto.setRecentRequest(RecentRequestType.CANCEL.name());
        }


        EntityModel entityModel = EntityModel.of(paymentSearchDto);
        entityModel.add(linkTo(PaymentSearchController.class).slash(paymentSearchDto).withSelfRel());
        entityModel.add(Link.of("/docs/index.html#resource-payment-get").withRel("profile"));

        return ResponseEntity.ok(entityModel);
    }

    private String maskingCardNo (String cardNo) {
        char[] maskingCharArr = new char[cardNo.length()];
        Arrays.fill(maskingCharArr,'*');
        char[] cardNoCharArr = cardNo.toCharArray();
        for(int i=0; i<cardNo.length();i++){
            if((i>=0 && i<6) || ( i>cardNo.length()-4)) {
                maskingCharArr[i] = cardNoCharArr[i];
            }
        }
        String maskedCardNo = new String(maskingCharArr);
        return maskedCardNo;
    }

    private ResponseEntity notFound() {
        return ResponseEntity.notFound().build();
    }



}
