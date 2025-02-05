package com.bigproject.fic2toon.play;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
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
            // DB에서 가져온 전체 URL (예: "https://aivlestorage.blob.core.windows.net/blob-28/27dfff0a95604194bd810574fdf89a99/final_outputs")
            String fullPath = logDto.getPath();
            // 마지막에 슬래시가 없으면 추가
            if (!fullPath.endsWith("/")) {
                fullPath += "/";
            }
            // 컨테이너 내부 경로 추출 (예: "27dfff0a95604194bd810574fdf89a99/final_outputs/")
            String basePath = extractBlobPath(fullPath);

            // basePath를 prefix로 사용하는 blob 목록을 가져옵니다.
            List<BlobItem> blobItems = new ArrayList<>();
            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(basePath)) {
                blobItems.add(blobItem);
            }
            // 파일 이름으로부터 숫자 값을 추출하여 정렬 (scene_1, scene_2, … scene_36 순)
            blobItems.sort((b1, b2) -> {
                int n1 = extractSceneNumber(b1.getName());
                int n2 = extractSceneNumber(b2.getName());
                return Integer.compare(n1, n2);
            });

            List<String> imagePaths = blobItems.stream()
                    .filter(blobItem -> blobItem.getName().matches("(?i).*\\.(jpg|jpeg|png|gif)$"))
                    .map(blobItem -> containerClient.getBlobClient(blobItem.getName()).getBlobUrl())
                    .collect(Collectors.toList());

            model.addAttribute("imagePaths", imagePaths);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("imagePaths", Collections.emptyList());
        }

        return "model/logdetail";
    }

    /**
     * DB에 저장된 전체 URL에서 컨테이너 내부 경로를 추출합니다.
     * 예: fullPath가 "https://aivlestorage.blob.core.windows.net/blob-28/27dfff0a95604194bd810574fdf89a99/final_outputs/"일 경우,
     *      반환값은 "27dfff0a95604194bd810574fdf89a99/final_outputs/"가 됩니다.
     */
    private String extractBlobPath(String fullPath) {
        String containerUrl = "https://aivlestorage.blob.core.windows.net/" + containerName + "/";
        if (fullPath.startsWith(containerUrl)) {
            return fullPath.substring(containerUrl.length());
        }
        throw new IllegalArgumentException("Invalid Blob URL: " + fullPath);
    }

    /**
     * Blob 이름에서 "scene_" 뒤의 숫자를 정수형으로 추출합니다.
     * 예: "27dfff0a95604194bd810574fdf89a99/final_outputs/scene_10.png" → 10
     */
    private int extractSceneNumber(String blobName) {
        int index = blobName.lastIndexOf("scene_");
        if (index != -1) {
            index += "scene_".length();
            int dotIndex = blobName.indexOf(".", index);
            if (dotIndex != -1) {
                try {
                    return Integer.parseInt(blobName.substring(index, dotIndex));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
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
