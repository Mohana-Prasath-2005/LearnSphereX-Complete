package com.learnspherex.payment; import java.util.*; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import lombok.RequiredArgsConstructor; import com.learnspherex.security.CurrentUserService;
@RestController @RequiredArgsConstructor public class PaymentController { private final PaymentService service; private final CurrentUserService currentUserService; @PostMapping("/api/fee-plans") @PreAuthorize("hasRole('ADMIN')") ResponseEntity<FeePlan> createPlan(@Valid @RequestBody PaymentDtos.CreateFeePlanRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.createPlan(r));} @PostMapping("/api/payments") @PreAuthorize("hasRole('ADMIN')") ResponseEntity<PaymentDtos.PaymentResponse> record(@Valid @RequestBody PaymentDtos.RecordPaymentRequest r,Authentication a,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(service.record(r,currentUserService.currentUser(a).getId(),req.getRemoteAddr()));} @GetMapping("/api/payments/student/{studentUserId}") @PreAuthorize("hasRole('ADMIN')") List<PaymentDtos.PaymentResponse> history(@PathVariable Long studentUserId){return service.history(studentUserId);}
 @GetMapping("/api/payments/student/{studentUserId}/outstanding") @PreAuthorize("hasRole('ADMIN')") PaymentDtos.OutstandingSummary outstanding(@PathVariable Long studentUserId){return service.outstandingForStudent(studentUserId);}
 @GetMapping("/api/payments/{id}/receipt") @PreAuthorize("hasRole('ADMIN')") ResponseEntity<byte[]> receipt(@PathVariable Long id){
  byte[] pdf=service.generateReceiptPdf(id);
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=receipt-"+id+".pdf")
          .body(pdf);
 }
}
