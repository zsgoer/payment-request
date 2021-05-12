package me.zsgoer.payment.paymentcancel;

import me.zsgoer.payment.paymentrequest.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel,Long> {
    List<PaymentCancel> findByManageNoOrderByCancelIdDesc(String manageNo);
}
