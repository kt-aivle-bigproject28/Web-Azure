package com.bigproject.fic2toon.play;

import com.bigproject.fic2toon.api.FastApiClient;
import com.bigproject.fic2toon.user.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/play")
public class PlayController {
    private final FastApiClient fastApiClient;
    private final PlayService playService;

    @GetMapping
    public String getPlayModel(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        return "model/playmodel";
    }

    @PostMapping
    public String textToWebtoon(@RequestPart("file") MultipartFile file,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            String response = fastApiClient.textToWebtoon(file);
            ObjectMapper objectMapper = new ObjectMapper();

            if (response.contains("error")) {
                Map<String, String> errorResponse = objectMapper.readValue(response,
                        new TypeReference<Map<String, String>>() {});
                model.addAttribute("error", errorResponse.get("error"));
                return "model/playmodel";
            }

            Map<String, Object> responseMap = objectMapper.readValue(response,
                    new TypeReference<Map<String, Object>>() {});

            List<String> imagePaths = (List<String>) responseMap.get("image_paths");
            session.setAttribute("imagePaths", imagePaths);
            redirectAttributes.addFlashAttribute("imagePaths", imagePaths);

            return "redirect:/play/savelog";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "파일 처리 실패: " + e.getMessage());
            return "model/playmodel";
        }
    }

    @GetMapping("/savelog")
    public String showSaveLog(HttpSession session, Model model) {
        List<String> imagePaths = (List<String>) session.getAttribute("imagePaths");
        model.addAttribute("imagePaths", imagePaths);
        return "model/savelog";
    }

    @PostMapping("/saveLog")
    public String saveLog(@ModelAttribute @Valid LogDto logDto,
                          HttpSession session,
                          Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);
        logDto.setUserUid(loginUserId);

        @SuppressWarnings("unchecked")
        List<String> imagePaths = (List<String>) session.getAttribute("imagePaths");
        System.out.println("SaveLog - Image paths from session: " + imagePaths);

        if (imagePaths == null || imagePaths.isEmpty()) {
            model.addAttribute("error", "이미지 경로가 없습니다.");
            return "model/playmodel";
        }

        String imagePath = imagePaths.get(0);
        String blobUrl = imagePath.substring(0, imagePath.lastIndexOf("/"));
        logDto.setPath(blobUrl);

        playService.savelog(logDto);
        // 세션에서 이미지 경로 정보 제거
        session.removeAttribute("imagePaths");

        return "redirect:/log";
    }
}