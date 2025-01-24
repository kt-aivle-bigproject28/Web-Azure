package com.bigproject.fic2toon;

import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final UserRepository userRepository;

    @GetMapping(value = {"/", "/home"})
    public String mainPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("loginUser");

        if (userId == null) {
            return "redirect:/login";
        }

        // 사용자 정보를 데이터베이스에서 조회
        User user = userRepository.findByUid(userId).orElse(null); // UserRepository 필요

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("userType", user.getType());
        model.addAttribute("userName", user.getName());

        if (user.getCompany() != null) {
            model.addAttribute("companyName", user.getCompany().getName());
        }

        return "board/home";
    }

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }
}
