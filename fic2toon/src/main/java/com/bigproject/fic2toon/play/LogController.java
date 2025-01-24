package com.bigproject.fic2toon.play;

import com.bigproject.fic2toon.board.BoardDto;
import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserRepository;
import com.bigproject.fic2toon.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final LogService logService;
    private final UserService userService;

    @GetMapping
    public String getLogList(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }

        User loginUser = userService.findByUid(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
        Long loginUserCompanyId = loginUser.getCompany().getId();

        model.addAttribute("user", loginUserId);
        model.addAttribute("logList", logService.getLogList(loginUserCompanyId));
        return "model/log";
    }

    @GetMapping("/{id}")
    public String getLogDetail(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId);
        model.addAttribute("log", logService.getLogById(id));

        LogDto logDto = logService.getLogById(id);
        String folderPath = logDto.getPath(); // LogDto의 path 속성 사용

        try {
            List<String> imagePaths = Files.list(Paths.get(folderPath))
                    .filter(Files::isRegularFile) // 파일만 가져오기
                    .filter(file -> file.toString().matches(".*\\.(jpg|jpeg|png|gif)$")) // 이미지 파일 필터링
                    .map(file -> "/uploads/log/1/" + file.getFileName()) // 프론트엔드에서 접근 가능한 URL로 매핑
                    .collect(Collectors.toList());

            model.addAttribute("imagePaths", imagePaths); // 이미지 경로 목록 전달
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("imagePaths", Collections.emptyList()); // 오류 발생 시 빈 목록 전달
        }

        return "model/logdetail";
    }
    @DeleteMapping("/{id}/delete")
    public String deleteLog(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId);

        LogDto log = logService.getLogById(id);

        // 권한 확인: 관리자 또는 작성자만 삭제 가능

        logService.deleteLog(id);

        // 삭제 후 게시글 목록으로 리다이렉트
        return "redirect:/log";
    }
}
