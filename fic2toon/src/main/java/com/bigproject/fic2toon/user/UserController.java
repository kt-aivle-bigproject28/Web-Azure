package com.bigproject.fic2toon.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupPage() {
        return "login/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupDto signupDto, Model model) {
        try {
            userService.createUser(signupDto, model); // 모델을 전달하지 않음
            return "redirect:/login"; // 회원가입 후 로그인 페이지로 리다이렉트
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "이미 사용 중인 ID입니다."); // 유일성 위반 시 메시지 설정
            model.addAttribute("signupDto", signupDto); // 입력한 데이터 유지
            return "login/signup"; // 오류 발생 시 회원가입 페이지로 돌아감
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage()); // 다른 예외 처리
            model.addAttribute("signupDto", signupDto); // 입력한 데이터 유지
            return "login/signup"; // 오류 발생 시 회원가입 페이지로 돌아감
        } catch (Exception e) {
            model.addAttribute("errorMessage", "서버 오류가 발생했습니다."); // 일반 오류 메시지 추가
            model.addAttribute("signupDto", signupDto); // 입력한 데이터 유지
            return "login/signup"; // 오류 발생 시 회원가입 페이지로 돌아감
        }
    }

    @PostMapping("/login")
    public String login(LoginRequestDto loginRequest, HttpSession session, Model model) {
        try {
            String token = userService.authenticate(loginRequest);
            session.setAttribute("loginUser", loginRequest.getUid());
            return "redirect:/"; // 로그인 성공 시 메인 페이지로 리다이렉트
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login/login"; // 로그인 실패 시 로그인 페이지로 돌아감
        } catch (Exception e) {
            model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
            return "login/login"; // 로그인 실패 시 로그인 페이지로 돌아감
        }
    }

    @GetMapping("/agree")
    public String showAgreementPage() {
        // 약관 동의 페이지를 반환
        return "login/agree";
    }

    @PostMapping("/agree")
    public String agreeToTerms() {
        // 사용자가 약관에 동의한 후 회원가입 페이지로 리다이렉트
        return "redirect:/signup"; // 회원가입 페이지로 리다이렉트
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "login/about"; // about pg 연결
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "login/contact"; // contact pg 연결
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 약관 동의 페이지를 반환
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/check-uid")
    @ResponseBody
    public Map<String, Boolean> checkUid(@RequestParam String uid) {
        boolean isAvailable = userService.isUidAvailable(uid);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return response;
    }
}