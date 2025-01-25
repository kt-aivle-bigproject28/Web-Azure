package com.bigproject.fic2toon.play;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

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
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);
        LogDto logDto = logService.getLogById(id);
        model.addAttribute("log", logDto);

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Blob 스토리지에서 해당 로그의 이미지들을 가져옴
            List<String> imagePaths = containerClient.listBlobs().stream()
                    .filter(blob -> blob.getName().startsWith("log/" + id + "/"))
                    .filter(blob -> blob.getName().matches(".*\\.(jpg|jpeg|png|gif)$"))
                    .map(blob -> containerClient.getBlobClient(blob.getName()).getBlobUrl())
                    .collect(Collectors.toList());

            model.addAttribute("imagePaths", imagePaths);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("imagePaths", Collections.emptyList());
        }

        return "model/logdetail";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteLog(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);
        LogDto log = logService.getLogById(id);

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // 해당 로그의 모든 이미지 삭제
            containerClient.listBlobs().stream()
                    .filter(blob -> blob.getName().startsWith("log/" + id + "/"))
                    .forEach(blob -> {
                        BlobClient blobClient = containerClient.getBlobClient(blob.getName());
                        blobClient.delete();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        logService.deleteLog(id);
        return "redirect:/log";
    }
}
