package me.zsgoer.payment.controller;

import me.zsgoer.payment.common.ErrorsResource;
import me.zsgoer.payment.common.ResponseDto;
import me.zsgoer.payment.common.SendStringDto;
import me.zsgoer.payment.paymentcancel.PaymentCancelValidator;
import me.zsgoer.payment.manageno.ManageNo;
import me.zsgoer.payment.manageno.ManageNoRepository;
import me.zsgoer.payment.manageno.RecentRequestType;
import me.zsgoer.payment.paymentcancel.PaymentCancel;
import me.zsgoer.payment.paymentcancel.PaymentCancelDto;
import me.zsgoer.payment.paymentcancel.PaymentCancelRepository;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value="/api/paymentcancel", produces= MediaTypes.HAL_JSON_VALUE)
public class PaymentCancelController {
    private final PaymentCancelRepository paymentCancelRepository;
    private final ManageNoRepository manageNoRepository;
    private final ModelMapper modelMapper ;
    private final PaymentCancelValidator paymentCancelValidator;

    public PaymentCancelController(ManageNoRepository manageNoRepository,
                                   PaymentCancelRepository paymentCancelRepository,
                                   ModelMapper modelMapper,
                                   PaymentCancelValidator paymentCancelValidator) {
        this.manageNoRepository = manageNoRepository;
        this.paymentCancelRepository = paymentCancelRepository;
        this.modelMapper = modelMapper;
        this.paymentCancelValidator = paymentCancelValidator;
    }

    @PostMapping
    public ResponseEntity cancelPaymentRequest(@RequestBody @Valid PaymentCancelDto paymentCancelDto, Errors errors) {
        if(errors.hasErrors()) return badRequest(errors);
        String orgManageNo = paymentCancelDto.getManageNo();

        //해당 관리 번호에 대한 존재 유뮤 검색.
        ManageNo currentPayment = this.manageNoRepository.findByManageNo(orgManageNo);
        if(currentPayment.getManageNo().isEmpty()) {
            return notFound();
        }
        //넘어온 파라미터 검증 (기존 값과 비교.)
        paymentCancelValidator.validate(currentPayment, paymentCancelDto, errors);
        if(errors.hasErrors()) return badRequest(errors);

        PaymentCancel paymentCancel = modelMapper.map(currentPayment, PaymentCancel.class);
        paymentCancel.updateDecCardInfo();         //기존 카드번호뭉치 복호화하여 String 제작위한 기반.
        paymentCancel.setOrgManageNo(orgManageNo); //원 관리번호 입력.
        paymentCancel.setSendingInfo(strBuildStringPaymentRequestData(paymentCancel)); //취소String 데이터 송신.

        modelMapper.map(paymentCancelDto,paymentCancel);

        paymentCancel = paymentCancelRepository.save(paymentCancel);
        calcManageNo(currentPayment,paymentCancel);
        this.manageNoRepository.save(currentPayment);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(PaymentRequestController.class).slash(orgManageNo);
        URI createdUri = selfLinkBuilder.toUri();
        ResponseDto responseDto = modelMapper.map(paymentCancel, ResponseDto.class);
        EntityModel entityModel = EntityModel.of(responseDto);
        entityModel.add(linkTo(PaymentCancelController.class).withSelfRel());
        entityModel.add(linkTo(PaymentSearchController.class).slash(orgManageNo).withRel("get-payment"));
        entityModel.add(Link.of("/docs/index.html#resources-payment-cancel").withRel("profile"));
        return ResponseEntity.created(createdUri).body(entityModel);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
    }

    private ResponseEntity notFound() {
        return ResponseEntity.notFound().build();
    }

    private String strBuildStringPaymentRequestData (PaymentCancel paymentCancel) {
        SendStringDto map = modelMapper.map(paymentCancel, SendStringDto.class);
        map.setDataGubun(RecentRequestType.CANCEL.name());

        return map.toGenerateSendString();
    }
    private void calcManageNo(ManageNo currentPayment, PaymentCancel paymentCancel) {
        int currentAmount = currentPayment.getAmount();
        int currentVat = currentPayment.getVat();
        int cancelAmount = paymentCancel.getAmount();
        int cancelVat = paymentCancel.getVat();

        currentPayment.setAmount(currentAmount-cancelAmount);
        currentPayment.setVat(currentVat-cancelVat);
    }

}
