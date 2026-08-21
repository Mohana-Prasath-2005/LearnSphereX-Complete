package com.learnspherex.certificate.controller;
import com.learnspherex.certificate.entity.Certificate; import com.learnspherex.certificate.service.CertificateService; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/certificates") @RequiredArgsConstructor
public class CertificateController {
 private final CertificateService service;
 @PostMapping("/generate") @ResponseStatus(HttpStatus.CREATED) public Certificate generate(@RequestParam Long studentId,@RequestParam Long courseId){return service.generate(studentId,courseId);}
 @GetMapping("/student/{studentId}") public List<Certificate> byStudent(@PathVariable Long studentId){return service.byStudent(studentId);}
 @GetMapping("/{certificateId}") public Certificate get(@PathVariable String certificateId){return service.get(certificateId);}
}
