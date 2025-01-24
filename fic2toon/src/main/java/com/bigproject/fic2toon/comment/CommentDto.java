package com.bigproject.fic2toon.comment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String userUid;
    private Long boardId;
    private String content;
    private LocalDateTime createdTime;
}