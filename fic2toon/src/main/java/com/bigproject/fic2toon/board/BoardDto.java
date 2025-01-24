package com.bigproject.fic2toon.board;

import com.bigproject.fic2toon.comment.CommentDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {
    private Long id;
    private String title;
    private String content;
    private Integer boardType; // 기존 유지
    private String boardTypeText; // 텍스트 변환 필드
    private String image;
    private String createdTime; // LocalDateTime → String으로 변경
    private String userUid;
    private List<CommentDto> comments;
}
