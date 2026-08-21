package com.learnspherex.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.learnspherex.auth.*;
import com.learnspherex.common.ApiException;
import com.learnspherex.payment.*;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.batch.entity.AttendanceStatus;
import com.learnspherex.batch.entity.BatchStatus;
import com.learnspherex.batch.repository.AttendanceRepository;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.examination.repository.ExamRepository;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.repository.StudentCourseRepository;
import com.learnspherex.student.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final AuthService authService;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final AttendanceRepository attendanceRepository;
    private final BatchRepository batchRepository;
    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    @GetMapping("/") String home(){ return "redirect:/login"; }
    @GetMapping("/login") String login(){ return "login"; }
    @GetMapping("/register") String registerForm(){ return "register"; }
    @PostMapping("/register") String register(@RequestParam String username,@RequestParam String email,@RequestParam String password,@RequestParam String firstName,@RequestParam String lastName,@RequestParam(required=false) String phone,HttpServletRequest req,Model model){ try{authService.register(new AuthDtos.RegisterRequest(username,email,password,firstName,lastName,phone,RoleName.STUDENT),req.getRemoteAddr());return "redirect:/login?registered";}catch(ApiException e){model.addAttribute("error",e.getMessage());return "register";} }
    @GetMapping("/dashboard") String dashboard(Authentication auth,Model model){
        user(model,auth);

        long activeCourses; long activeBatches; double attendance; long upcomingExams;

        var currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        var studentOpt = currentUser != null ? studentRepository.findByUserId(currentUser.getId()) : java.util.Optional.<com.learnspherex.student.entity.Student>empty();

        if (studentOpt.isPresent()) {
            Long studentId = studentOpt.get().getId();
            var enrollments = studentCourseRepository.findByStudentId(studentId);
            activeCourses = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
            activeBatches = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .map(e -> e.getBatch().getId()).distinct().count();
            var records = attendanceRepository.findByStudentId(studentId);
            attendance = records.isEmpty() ? 0.0 : Math.round(records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                    .count() * 10000.0 / records.size()) / 100.0;
            Set<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toSet());
            upcomingExams = examRepository.findAll().stream()
                    .filter(e -> courseIds.contains(e.getCourseId()) && e.getStartAt().isAfter(LocalDateTime.now()))
                    .count();
        } else {
            // Staff (admin/trainer/hr/evaluator) view: system-wide figures rather than a specific student's.
            activeCourses = courseRepository.count();
            activeBatches = batchRepository.findAll().stream().filter(b -> b.getBatchStatus() == BatchStatus.ACTIVE).count();
            var records = attendanceRepository.findAll();
            attendance = records.isEmpty() ? 0.0 : Math.round(records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                    .count() * 10000.0 / records.size()) / 100.0;
            upcomingExams = examRepository.findAll().stream()
                    .filter(e -> e.getStartAt().isAfter(LocalDateTime.now()))
                    .count();
        }

        model.addAttribute("activeCourses", activeCourses);
        model.addAttribute("activeBatches", activeBatches);
        model.addAttribute("attendancePercentage", attendance);
        model.addAttribute("upcomingExams", upcomingExams);

        return "dashboard";
    }
    @GetMapping("/users") @PreAuthorize("hasRole('ADMIN')") String users(Authentication auth,Model model){user(model,auth);model.addAttribute("users",authService.list());return "users";}
    @GetMapping("/users/new") @PreAuthorize("hasRole('ADMIN')") String newUser(Authentication auth,Model model){user(model,auth);model.addAttribute("roles",RoleName.values());return "create-user";}
    @PostMapping("/users/new") @PreAuthorize("hasRole('ADMIN')") String createUser(@RequestParam String username,@RequestParam String email,@RequestParam String password,@RequestParam String firstName,@RequestParam String lastName,@RequestParam(required=false) String phone,@RequestParam RoleName role,HttpServletRequest req,Authentication auth,Model model){try{authService.createStaffUser(new AuthDtos.RegisterRequest(username,email,password,firstName,lastName,phone,role),req.getRemoteAddr(),currentUserService.currentUser(auth).getId());return "redirect:/users?created";}catch(ApiException e){user(model,auth);model.addAttribute("roles",RoleName.values());model.addAttribute("error",e.getMessage());return "create-user";}}
    @PostMapping("/users/{id}/status") @PreAuthorize("hasRole('ADMIN')") String status(@PathVariable Long id,@RequestParam boolean active,Authentication auth,HttpServletRequest req){authService.setStatus(id,active,currentUserService.currentUser(auth).getId(),req.getRemoteAddr());return "redirect:/users";}
    @GetMapping("/fee-plans") @PreAuthorize("hasRole('ADMIN')") String feePlans(Authentication auth,Model model){user(model,auth);return "fee-plans";}
    @PostMapping("/fee-plans") @PreAuthorize("hasRole('ADMIN')") String createPlan(@RequestParam Long courseId,@RequestParam String name,@RequestParam BigDecimal totalAmount,@RequestParam BigDecimal amount1,@RequestParam String due1,@RequestParam BigDecimal amount2,@RequestParam String due2){paymentService.createPlan(new PaymentDtos.CreateFeePlanRequest(courseId,name,totalAmount,List.of(new PaymentDtos.InstallmentRequest(1,amount1,LocalDate.parse(due1)),new PaymentDtos.InstallmentRequest(2,amount2,LocalDate.parse(due2)))));return "redirect:/fee-plans?success";}
    @GetMapping("/payments") @PreAuthorize("hasRole('ADMIN')") String payments(Authentication auth,Model model){user(model,auth);model.addAttribute("methods",PaymentMethod.values());return "payments";}
    @PostMapping("/payments") @PreAuthorize("hasRole('ADMIN')") String payment(@RequestParam Long studentUserId,@RequestParam Long installmentId,@RequestParam BigDecimal amount,@RequestParam PaymentMethod method,@RequestParam String transactionReference,Authentication auth,HttpServletRequest req){PaymentDtos.PaymentResponse result=paymentService.record(new PaymentDtos.RecordPaymentRequest(studentUserId,installmentId,amount,method,transactionReference),currentUserService.currentUser(auth).getId(),req.getRemoteAddr());return "redirect:/payments?receipt="+result.receiptNumber();}
    private void user(Model m,Authentication a){m.addAttribute("username",a.getName());m.addAttribute("roles",a.getAuthorities().stream().map(x->x.getAuthority().replace("ROLE_","")).toList());}
}
