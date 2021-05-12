package me.zsgoer.payment.controller;

import me.zsgoer.payment.common.ErrorsResource;
import me.zsgoer.payment.common.ResponseDto;
import me.zsgoer.payment.common.SendStringDto;
import me.zsgoer.payment.manageno.ManageNo;
import me.zsgoer.payment.manageno.ManageNoRepository;
import me.zsgoer.payment.manageno.RecentRequestType;
import me.zsgoer.payment.paymentrequest.PaymentRequest;
import me.zsgoer.payment.paymentrequest.PaymentRequestRepository;
import me.zsgoer.payment.paymentrequest.PaymentRequestDto;
import me.zsgoer.payment.paymentrequest.PaymentRequestValidator;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Controller
@RequestMapping(value="/api/paymentrequest", produces= MediaTypes.HAL_JSON_VALUE)
public class PaymentRequestController {

    private final PaymentRequestRepository paymentRequestRepository;

    private final ManageNoRepository manageNoRepository;

    private final ModelMapper modelMapper ;
    private final PaymentRequestValidator paymentRequestValidator;

    public PaymentRequestController(PaymentRequestRepository paymentRepository,
                                    ManageNoRepository manageNoRepository,ModelMapper modelMapper, PaymentRequestValidator paymentRequestValidator) {
        this.paymentRequestRepository = paymentRepository;
        this.manageNoRepository = manageNoRepository;
        this.modelMapper = modelMapper;
        this.paymentRequestValidator = paymentRequestValidator;
    }

    @PostMapping
    public ResponseEntity registPaymentRequest(@RequestBody @Valid PaymentRequestDto paymentRequestDto, Errors errors) {
        if(errors.hasErrors()) return badRequest(errors);
        paymentRequestValidator.validate(paymentRequestDto,errors);
        if(errors.hasErrors()) return badRequest(errors);
        PaymentRequest paymentRequest = modelMapper.map(paymentRequestDto, PaymentRequest.class);
        paymentRequest.updateEncCardInfo();

        //관리번호 추출을 위한 SEQUENCE 추출
        ManageNo manageNo = manageNoRepository.save(new ManageNo());
        //관리번호
        String strManageNo = String.format("%020d",manageNo.getId());

        //관리번호를 요청이력에 SET
        paymentRequest.setManageNo(strManageNo);
        //전송String Data 생성.
        paymentRequest.setSendingInfo(strBuildStringPaymentRequestData(paymentRequest));
        //정상요청이력 저장.
        PaymentRequest newPaymentRequest = paymentRequestRepository.save(paymentRequest);

        modelMapper.map(newPaymentRequest,manageNo);
        manageNo.setRecentRequest(RecentRequestType.PAYMENT.name());
        manageNo = manageNoRepository.save(manageNo);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(PaymentRequestController.class).slash(strManageNo);
        URI createdUri = selfLinkBuilder.toUri();

        ResponseDto responseDto = modelMapper.map(newPaymentRequest, ResponseDto.class);
        EntityModel entityModel = EntityModel.of(responseDto);
        entityModel.add(linkTo(PaymentRequestController.class).withSelfRel());
        entityModel.add(linkTo(PaymentSearchController.class).slash(strManageNo).withRel("get-payment"));
        entityModel.add(Link.of("/docs/index.html#resources-payment-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(entityModel);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
    }

    private ResponseEntity notFound() {
        return ResponseEntity.notFound().build();
    }

    private String strBuildStringPaymentRequestData (PaymentRequest paymentRequest) {
        SendStringDto map = modelMapper.map(paymentRequest, SendStringDto.class);
        map.setDataGubun(RecentRequestType.PAYMENT.name());

        return map.toGenerateSendString();
    }
}
