package me.zsgoer.payment.paymentrequest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest,String> {
}
