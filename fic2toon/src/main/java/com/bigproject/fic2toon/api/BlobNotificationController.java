package com.bigproject.fic2toon.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BlobNotificationController {

    @PostMapping("/blob-notify")
    public ResponseEntity<String> receiveBlobNotification(@RequestBody Map<String, String> request) {
        String fileUrl = request.get("fileUrl");
        if (fileUrl == null || fileUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("fileUrl is required");
        }

        System.out.println("✅ 업로드된 파일 URL 수신: " + fileUrl);
        // 여기서 필요한 로직을 추가 (예: DB 저장, 후속 처리 등)

        return ResponseEntity.ok("✅ 파일 업로드 알림 수신 완료");
    }
}