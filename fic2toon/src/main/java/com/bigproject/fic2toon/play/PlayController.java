package com.bigproject.fic2toon.play;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/play")
public class PlayController {
    private final PlayService playService;

    // 1. 파일 업로드 페이지 (playmodel.html)
    @GetMapping
    public String getPlayModel(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        return "model/playmodel";
    }

    // 2. 처리 진행 페이지 (processing.html)
    @GetMapping("/processing")
    public String showProcessing(HttpSession session, Model model) {
        // 파일 업로드 이후(클라이언트 측에서 sessionStorage에 저장된 fileData를 사용)
        // 별도의 파라미터나 세션 데이터 없이 페이지를 렌더링합니다.
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        return "model/processing";
    }

    @GetMapping("/savelog")
    public String showSaveLog(HttpSession session, Model model) {
        // 파일 업로드 이후(클라이언트 측에서 sessionStorage에 저장된 fileData를 사용)
        // 별도의 파라미터나 세션 데이터 없이 페이지를 렌더링합니다.
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        return "model/savelog";
    }

    // 3. 결과 저장 처리 (savelog.html에서 최종 저장 요청 시)
    @PostMapping("/savelog")
    public String saveLog(@ModelAttribute @Valid LogDto logDto,
                          @RequestParam String imagePaths, // 클라이언트에서 전달받은 폴더 URL, 즉 Blob Storage의 폴더 URL 등
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }

        logDto.setUserUid(loginUserId);
        logDto.setPath(imagePaths);

        playService.saveLog(logDto);
        return "redirect:/log";
    }
}
