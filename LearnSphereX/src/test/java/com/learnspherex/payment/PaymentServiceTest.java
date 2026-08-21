package com.learnspherex.payment;

import com.learnspherex.audit.AuditService;
import com.learnspherex.common.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private FeePlanRepository plans;
    @Mock private InstallmentRepository installments;
    @Mock private PaymentRepository payments;
    @Mock private AuditService audit;

    private PaymentService service() {
        return new PaymentService(plans, installments, payments, audit);
    }

    private Installment installment(BigDecimal amount) {
        FeePlan plan = new FeePlan(1L, "JFS Plan", BigDecimal.valueOf(1000));
        return new Installment(plan, 1, amount, LocalDate.now().plusDays(30));
    }

    // Two payments that would each individually pass a naive face-value check
    // must not be allowed to together overpay the installment.
    @Test
    void paymentExceedingRemainingBalanceAfterPriorPaymentsIsRejected() {
        Installment inst = installment(BigDecimal.valueOf(100));
        when(installments.findById(10L)).thenReturn(Optional.of(inst));
        Payment priorPayment = new Payment(1L, inst, BigDecimal.valueOf(60), PaymentMethod.CASH, "TXN-PRIOR");
        when(payments.findByInstallmentId(10L)).thenReturn(List.of(priorPayment));

        var request = new PaymentDtos.RecordPaymentRequest(1L, 10L, BigDecimal.valueOf(50), PaymentMethod.CASH, "TXN-NEW");

        ApiException ex = assertThrows(ApiException.class, () -> service().record(request, 99L, "127.0.0.1"));
        assertTrue(ex.getMessage().contains("exceeds the remaining balance"));
        verify(payments, never()).save(any());
    }

    @Test
    void paymentExactlyMatchingRemainingBalanceSucceeds() {
        Installment inst = installment(BigDecimal.valueOf(100));
        when(installments.findById(10L)).thenReturn(Optional.of(inst));
        Payment priorPayment = new Payment(1L, inst, BigDecimal.valueOf(60), PaymentMethod.CASH, "TXN-PRIOR");
        when(payments.findByInstallmentId(10L)).thenReturn(List.of(priorPayment));

        var request = new PaymentDtos.RecordPaymentRequest(1L, 10L, BigDecimal.valueOf(40), PaymentMethod.CASH, "TXN-NEW");

        var response = service().record(request, 99L, "127.0.0.1");

        assertEquals(0, BigDecimal.valueOf(40).compareTo(response.amount()));
        verify(payments).save(any());
    }

    @Test
    void duplicateTransactionReferenceIsRejected() {
        when(payments.existsByTransactionReference("TXN-DUP")).thenReturn(true);
        var request = new PaymentDtos.RecordPaymentRequest(1L, 10L, BigDecimal.TEN, PaymentMethod.CASH, "TXN-DUP");

        ApiException ex = assertThrows(ApiException.class, () -> service().record(request, 99L, "127.0.0.1"));
        assertTrue(ex.getMessage().contains("Transaction reference already used"));
        verifyNoInteractions(installments);
    }
}
