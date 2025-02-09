package com.bigproject.fic2toon.play;

import com.bigproject.fic2toon.board.Board;
import com.bigproject.fic2toon.board.BoardDto;
import com.bigproject.fic2toon.company.Company;
import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PlayService {
    private final UserService userService;
    private final LogRepository logRepository;

    public void saveLog(LogDto logDto){
        User user = userService.findByUid(logDto.getUserUid())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        Company company = user.getCompany();

        Log log = Log.builder()
                .title(logDto.getTitle())
                .user(user)
                .company(company)
                .path(logDto.getPath())
                .isPublic(logDto.getIsPublic())
                .build();

        logRepository.save(log); // 게시글 저장
    }
}
