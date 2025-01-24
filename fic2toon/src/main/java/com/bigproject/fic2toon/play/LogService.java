package com.bigproject.fic2toon.play;

import com.bigproject.fic2toon.board.BoardDto;
import com.bigproject.fic2toon.company.CompanyService;
import com.bigproject.fic2toon.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;
    private final UserService userService;
    private final CompanyService companyService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<LogDto> getLogList(Long loginUserCompanyId) {
        return logRepository.findAll().stream()
                .filter(log -> log.getIsPublic() == 0 ||
                        (log.getIsPublic() == 1 && log.getCompany().getId().equals(loginUserCompanyId)))
                .map(log -> {
                    String userUid = null;
                    String companyName = null;

                    if (log.getUser() != null) {
                        userUid = log.getUser().getUid(); // 작성자 UID 조회
                        companyName = log.getCompany().getName();
                    }

                    String formattedCreatedTime = log.getCreatedTime() != null
                            ? log.getCreatedTime().format(FORMATTER)
                            : "알 수 없음";

                    return new LogDto(
                            log.getId(),
                            log.getTitle(),
                            log.getPath(),
                            userUid,
                            companyName,
                            formattedCreatedTime,
                            log.getIsPublic()
                    );
                })
                .collect(Collectors.toList());
    }


    public LogDto getLogById(Long id) {
        return logRepository.findById(id)
                .map(log -> {
                    // createdTime을 문자열로 포맷
                    String userUid = null;
                    String companyName = null;

                    if (log.getUser() != null) {
                        userUid = userService.findById(log.getUser().getId()).getUid(); // 작성자 UID 조회
                        companyName = companyService.findById(log.getCompany().getId()).getName();
                    }

                    // createdTime을 문자열로 포맷
                    String formattedCreatedTime = log.getCreatedTime() != null
                            ? log.getCreatedTime().format(FORMATTER)
                            : "알 수 없음";


                    return new LogDto(
                            log.getId(),
                            log.getTitle(),
                            log.getPath(),
                            userUid,
                            companyName,
                            formattedCreatedTime, // 포맷된 문자열 전달
                            log.getIsPublic()
                    );
                })
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    public void deleteLog(Long id) {
        if (!logRepository.existsById(id)) {
            throw new RuntimeException("삭제하려는 게시글이 존재하지 않습니다.");
        }
        logRepository.deleteById(id);
    }
}
