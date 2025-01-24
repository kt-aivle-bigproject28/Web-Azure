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
    private final UserService userService;

    @GetMapping
    public String getPlayModel(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId); // 사용자 타입 추가
        return "model/playmodel"; // 게시판 뷰 반환
    }

//    @PostMapping("/text_to_webtoon")
//    public String textToWebtoon(@RequestParam("text") MultipartFile text, Model model) throws IOException {
//        try {
//            // FastApiClient를 통해 API 호출
//            String response = fastApiClient.textToWebtoon(text);
//
//            // ObjectMapper를 사용하여 JSON 문자열을 Map으로 변환
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            // 응답이 오류인지 확인
//            if (response.contains("error")) {
//                Map<String, String> errorResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>(){});
//                model.addAttribute("error", errorResponse.get("error")); // 에러 메시지 모델에 추가
//            } else {
//                Map<String, List<String>> responseMap = objectMapper.readValue(response, new TypeReference<Map<String, List<String>>>(){});
//                List<String> imagePaths = responseMap.get("image_paths");
//                model.addAttribute("imagePaths", imagePaths); // 이미지 경로를 모델에 추가
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            model.addAttribute("error", "파일 처리 실패: " + e.getMessage());
//        }
//        return "model/playmodel"; // 뷰 반환
//    }

    @PostMapping("/text_to_webtoon")
    public String textToWebtoon(@RequestParam("text") MultipartFile text, Model model) throws IOException {
        try {
            // FastApiClient를 통해 API 호출
            String response = fastApiClient.textToWebtoon(text);

            // ObjectMapper를 사용하여 JSON 문자열을 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            if (response.contains("error")) {
                Map<String, String> errorResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {});
                model.addAttribute("error", errorResponse.get("error"));
                return "model/playmodel"; // 에러 발생 시 원래 페이지로
            } else {
                Map<String, List<String>> responseMap = objectMapper.readValue(response, new TypeReference<Map<String, List<String>>>() {});
                List<String> imagePaths = responseMap.get("image_paths");
                model.addAttribute("imagePaths", imagePaths); // 이미지 경로 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "파일 처리 실패: " + e.getMessage());
            return "model/playmodel"; // 에러 발생 시 원래 페이지로
        }

        return "model/savelog"; // 성공 시 savelog.html로 이동
    }

    @PostMapping("/saveLog")
    public String saveLog(@ModelAttribute @Valid LogDto logDto,
                          HttpSession session,
                          Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");

        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId); // 사용자 타입 추가
        logDto.setUserUid(loginUserId);

        String uploadDir = "C:/uploads/log/1/";
        logDto.setPath(uploadDir);

        playService.savelog(logDto);

        return "redirect:/log"; // 저장 완료 후 게시판으로 이동
    }
}
