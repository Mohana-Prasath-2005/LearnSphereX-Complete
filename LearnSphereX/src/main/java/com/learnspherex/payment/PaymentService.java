package com.learnspherex.payment;

import java.io.ByteArrayOutputStream;
import java.math.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.learnspherex.common.ApiException;
import com.learnspherex.audit.AuditService;
import com.learnspherex.exception.ResourceNotFoundException;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
public class PaymentService {
	private final FeePlanRepository plans;
	private final InstallmentRepository installments;
	private final PaymentRepository payments;
	private final AuditService audit;

	@Transactional
	public FeePlan createPlan(PaymentDtos.CreateFeePlanRequest r) {
		BigDecimal sum = r.installments().stream().map(PaymentDtos.InstallmentRequest::amount).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		if (sum.compareTo(r.totalAmount()) != 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "Installments must equal total amount");
		FeePlan p = new FeePlan(r.courseId(), r.name(), r.totalAmount());
		r.installments().forEach(i -> p.addInstallment(new Installment(p, i.number(), i.amount(), i.dueDate())));
		return plans.save(p);
	}

	@Transactional
	public PaymentDtos.PaymentResponse record(PaymentDtos.RecordPaymentRequest r, Long actorId, String ip) {
		if (payments.existsByTransactionReference(r.transactionReference()))
			throw new ApiException(HttpStatus.CONFLICT, "Transaction reference already used");
		Installment i = installments.findById(r.installmentId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Installment not found"));

		// Compare against what's actually still owed on this installment, not
		// just the installment's face amount - otherwise two payments could
		// each individually pass the check while together overpaying it.
		BigDecimal alreadyPaid = payments.findByInstallmentId(r.installmentId()).stream()
				.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal remaining = i.getAmount().subtract(alreadyPaid);
		if (r.amount().compareTo(remaining) > 0)
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"Payment of " + r.amount() + " exceeds the remaining balance of " + remaining + " on this installment");

		Payment p = new Payment(r.studentUserId(), i, r.amount(), r.method(), r.transactionReference());
		PaymentReceipt receipt = new PaymentReceipt(p,
				"RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		p.attachReceipt(receipt);
		payments.save(p);
		audit.record(actorId, "CREATE", "Payment", p.getId(), ip,
				"Payment recorded for student user " + r.studentUserId());
		return view(p);
	}

	@Transactional(readOnly = true)
	public List<PaymentDtos.PaymentResponse> history(Long studentUserId) {
		return payments.findByStudentUserIdOrderByPaidAtDesc(studentUserId).stream().map(this::view).toList();
	}

	/**
	 * Pending balance for a student: every installment belonging to a fee plan
	 * this student has made at least one payment against (the only signal this
	 * schema has for "this plan applies to them"), minus what they've already
	 * paid toward each one.
	 */
	@Transactional(readOnly = true)
	public PaymentDtos.OutstandingSummary outstandingForStudent(Long studentUserId) {
		List<Payment> studentPayments = payments.findByStudentUserIdOrderByPaidAtDesc(studentUserId);

		Set<FeePlan> plansForStudent = new LinkedHashSet<>();
		for (Payment p : studentPayments) {
			plansForStudent.add(p.getInstallment().getFeePlan());
		}

		List<PaymentDtos.OutstandingInstallment> rows = new ArrayList<>();
		BigDecimal totalPending = BigDecimal.ZERO;

		for (FeePlan plan : plansForStudent) {
			for (Installment inst : plan.getInstallments()) {
				BigDecimal paid = payments.findByStudentUserIdAndInstallmentId(studentUserId, inst.getId()).stream()
						.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal pending = inst.getAmount().subtract(paid);
				if (pending.compareTo(BigDecimal.ZERO) > 0) {
					rows.add(new PaymentDtos.OutstandingInstallment(inst.getId(), plan.getCourseId(),
							inst.getInstallmentNumber(), inst.getDueDate(), inst.getAmount(), paid, pending));
					totalPending = totalPending.add(pending);
				}
			}
		}

		return new PaymentDtos.OutstandingSummary(studentUserId, rows, totalPending);
	}

	/**
	 * System-wide pending fees, for the admin dashboard. Only counts
	 * (student, installment) pairs with at least one payment on record, since
	 * that's the only way this schema knows a student is party to a plan.
	 */
	@Transactional(readOnly = true)
	public BigDecimal totalOutstanding() {
		Map<String, BigDecimal> paidByStudentInstallment = new HashMap<>();
		for (Payment p : payments.findAll()) {
			String key = p.getStudentUserId() + ":" + p.getInstallment().getId();
			paidByStudentInstallment.merge(key, p.getAmount(), BigDecimal::add);
		}
		BigDecimal total = BigDecimal.ZERO;
		for (Map.Entry<String, BigDecimal> entry : paidByStudentInstallment.entrySet()) {
			Long installmentId = Long.valueOf(entry.getKey().split(":")[1]);
			Installment inst = installments.findById(installmentId).orElse(null);
			if (inst == null) continue;
			BigDecimal pending = inst.getAmount().subtract(entry.getValue());
			if (pending.compareTo(BigDecimal.ZERO) > 0) {
				total = total.add(pending);
			}
		}
		return total;
	}

	@Transactional(readOnly = true)
	public byte[] generateReceiptPdf(Long paymentId) {
		Payment p = payments.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, out);
			document.open();
			document.add(new Paragraph("LearnSphereX - Payment Receipt"));
			document.add(new Paragraph(" "));
			document.add(new Paragraph("Receipt Number: " + p.getReceipt().getReceiptNumber()));
			document.add(new Paragraph("Student User ID: " + p.getStudentUserId()));
			document.add(new Paragraph("Installment #" + p.getInstallment().getInstallmentNumber()
					+ " (Fee Plan: " + p.getInstallment().getFeePlan().getName() + ")"));
			document.add(new Paragraph("Amount Paid: " + p.getAmount()));
			document.add(new Paragraph("Payment Method: " + p.getMethod()));
			document.add(new Paragraph("Transaction Reference: " + p.getTransactionReference()));
			document.add(new Paragraph("Status: " + p.getStatus()));
			document.add(new Paragraph("Paid At: " + p.getPaidAt()));
			document.close();
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate receipt PDF: " + ex.getMessage());
		}
		return out.toByteArray();
	}

	private PaymentDtos.PaymentResponse view(Payment p) {
		return new PaymentDtos.PaymentResponse(p.getId(), p.getReceipt().getReceiptNumber(), p.getStudentUserId(),
				p.getInstallment().getId(), p.getAmount(), p.getMethod(), p.getStatus(), p.getTransactionReference(),
				p.getPaidAt());
	}
}
