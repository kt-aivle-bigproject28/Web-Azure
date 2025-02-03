package com.bigproject.fic2toon.play;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
                          @RequestParam String imagePaths, // JSON 배열 문자열 형태로 전달됨
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);

        try {
            // imagePaths를 JSON 배열(List<String>)으로 파싱
            ObjectMapper mapper = new ObjectMapper();
            List<String> urls = mapper.readValue(imagePaths, new TypeReference<List<String>>() {});
            if (urls != null && !urls.isEmpty()) {
                // 첫 번째 URL에서 기본 경로 추출
                String firstUrl = urls.get(0);
                int index = firstUrl.indexOf("/scene_");
                if (index != -1) {
                    String basePath = firstUrl.substring(0, index);
                    // 필요하다면 뒤에 슬래시 추가 ("final_outputs/")
                    logDto.setPath(basePath);
                } else {
                    // /scene_이 발견되지 않으면 첫 번째 URL 전체 사용
                    logDto.setPath(firstUrl);
                }
            } else {
                logDto.setPath("");
            }
        } catch (Exception e) {
            // 파싱중 예외 처리
            redirectAttributes.addFlashAttribute("error", "이미지 경로 처리 중 오류 발생");
            return "redirect:/play";
        }

        logDto.setUserUid(loginUserId);
        playService.saveLog(logDto);
        return "redirect:/log";
    }
}