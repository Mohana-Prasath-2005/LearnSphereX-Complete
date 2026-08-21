package com.learnspherex.payment.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnspherex.notification.event.NotificationEvent;
import com.learnspherex.payment.Installment;
import com.learnspherex.payment.InstallmentRepository;
import com.learnspherex.payment.Payment;
import com.learnspherex.payment.PaymentRepository;

/**
 * Notifies students, once a day, about installments due within the next 3
 * days that they still owe money on. There's no direct student-to-fee-plan
 * enrollment link in this schema, so a student having made at least one
 * payment against a fee plan is used as the signal that the plan applies
 * to them.
 */
@Component
public class PaymentDueReminderScheduler {

    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentDueReminderScheduler(
            InstallmentRepository installmentRepository,
            PaymentRepository paymentRepository,
            ApplicationEventPublisher eventPublisher) {

        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void remindUpcomingDues() {

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(3);

        List<Installment> installments = installmentRepository.findAll();

        for (Installment installment : installments) {

            if (installment.getDueDate() == null
                    || installment.getDueDate().isBefore(today)
                    || installment.getDueDate().isAfter(threshold)) {
                continue;
            }

            Set<Long> studentUserIds = new HashSet<>();
            for (Installment sibling : installment.getFeePlan().getInstallments()) {
                for (Payment p : paymentRepository.findByInstallmentId(sibling.getId())) {
                    studentUserIds.add(p.getStudentUserId());
                }
            }

            for (Long studentUserId : studentUserIds) {

                BigDecimal paid = paymentRepository
                        .findByStudentUserIdAndInstallmentId(studentUserId, installment.getId())
                        .stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (paid.compareTo(installment.getAmount()) < 0) {
                    eventPublisher.publishEvent(new NotificationEvent(
                            studentUserId,
                            null,
                            "Payment Due",
                            "Installment #" + installment.getInstallmentNumber()
                                    + " (" + installment.getAmount() + ") is due on " + installment.getDueDate(),
                            "PAYMENT_DUE"));
                }
            }
        }
    }
}
