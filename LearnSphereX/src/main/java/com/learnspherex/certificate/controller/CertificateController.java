package com.learnspherex.certificate.controller;
import com.learnspherex.certificate.entity.Certificate; import com.learnspherex.certificate.service.CertificateService; import com.learnspherex.security.CurrentUserService; import com.learnspherex.student.repository.StudentRepository; import com.learnspherex.exception.ResourceNotFoundException; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/certificates") @RequiredArgsConstructor
public class CertificateController {
 private final CertificateService service;
 private final CurrentUserService currentUserService;
 private final StudentRepository students;

 @PostMapping("/generate") @ResponseStatus(HttpStatus.CREATED) public Certificate generate(@RequestParam Long studentId,@RequestParam Long courseId){return service.generate(studentId,courseId);}
 @GetMapping("/student/{studentId}") public List<Certificate> byStudent(@PathVariable Long studentId,Authentication authentication){
  Long ownerUserId=students.findById(studentId).orElseThrow(()->new ResourceNotFoundException("Student not found: "+studentId)).getUserId();
  currentUserService.assertOwnerOrRole(authentication,ownerUserId,"ADMIN","TRAINER");
  return service.byStudent(studentId);
 }
 @GetMapping("/{certificateId}") public Certificate get(@PathVariable String certificateId){return service.get(certificateId);}

 @GetMapping("/{id}/download") public ResponseEntity<byte[]> download(@PathVariable Long id,Authentication authentication){
  Certificate c=service.getById(id);
  Long ownerUserId=students.findById(c.getStudentId()).map(s->s.getUserId()).orElse(null);
  currentUserService.assertOwnerOrRole(authentication,ownerUserId,"ADMIN","TRAINER");
  byte[] pdf=service.generatePdf(id);
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+c.getCertificateId()+".pdf")
          .body(pdf);
 }
}
