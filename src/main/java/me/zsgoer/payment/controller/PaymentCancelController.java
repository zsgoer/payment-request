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

        //?????? ?????? ????????? ?????? ?????? ?????? ??????.
        ManageNo currentPayment = this.manageNoRepository.findByManageNo(orgManageNo);
        if(currentPayment.getManageNo().isEmpty()) {
            return notFound();
        }
        //????????? ???????????? ?????? (?????? ?????? ??????.)
        paymentCancelValidator.validate(currentPayment, paymentCancelDto, errors);
        if(errors.hasErrors()) return badRequest(errors);

        PaymentCancel paymentCancel = modelMapper.map(currentPayment, PaymentCancel.class);
        paymentCancel.updateDecCardInfo();         //?????? ?????????????????? ??????????????? String ???????????? ??????.
        paymentCancel.setOrgManageNo(orgManageNo); //??? ???????????? ??????.
        paymentCancel.setSendingInfo(strBuildStringPaymentRequestData(paymentCancel)); //??????String ????????? ??????.

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
