package com.bigproject.fic2toon.board;

import com.bigproject.fic2toon.comment.Comment;
import com.bigproject.fic2toon.comment.CommentDto;
import com.bigproject.fic2toon.comment.CommentService;
import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserService userService;
    private final CommentService commentService;

    // 날짜 포맷터 추가
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void createBoard(BoardDto boardDto) {
        User user = userService.findByUid(boardDto.getUserUid())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        Board board = Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .boardType(boardDto.getBoardType()) // int로 설정
                .image(boardDto.getImage()) // 이미지 설정 (null 가능)
                .user(user) // 작성자 설정
                .build();

        boardRepository.save(board); // 게시글 저장
    }



    public List<BoardDto> getBoardList() {
        return boardRepository.findAll().stream()
                .map(board -> {
                    String userUid = null;
                    if (board.getUser() != null) {
                        userUid = userService.findById(board.getUser().getId()).getUid(); // 작성자 UID 조회
                    }

                    // createdTime을 문자열로 포맷
                    String formattedCreatedTime = board.getCreatedTime() != null
                            ? board.getCreatedTime().format(FORMATTER)
                            : "알 수 없음";

                    List<Comment> comments = commentService.findByBoardId(board.getId());

                    List<CommentDto> commentDtos = comments.stream()
                            .map(comment -> new CommentDto(
                                    comment.getId(),
                                    comment.getUser().getUid(),
                                    comment.getBoard().getId(),
                                    comment.getContent(),
                                    comment.getCreatedTime()
                            ))
                            .collect(Collectors.toList());

                    return new BoardDto(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getBoardType(), // DB 정수 값
                            getBoardTypeText(board.getBoardType()), // 변환된 텍스트
                            board.getImage(),
                            formattedCreatedTime, // 포맷된 문자열 전달
                            userUid,
                            commentDtos
                    );
                })
                .collect(Collectors.toList());
    }

    public BoardDto getBoardById(Long id) {
        return boardRepository.findById(id)
                .map(board -> {
                    // createdTime을 문자열로 포맷
                    String formattedCreatedTime = board.getCreatedTime() != null
                            ? board.getCreatedTime().format(FORMATTER)
                            : "알 수 없음";

                    String userUid = null;
                    if (board.getUser() != null) {
                        userUid = userService.findById(board.getUser().getId()).getUid(); // 작성자 UID 조회
                    }

                    List<Comment> comments = commentService.findByBoardId(board.getId());

                    List<CommentDto> commentDtos = comments.stream()
                            .map(comment -> new CommentDto(
                                    comment.getId(),
                                    comment.getUser().getUid(),
                                    comment.getBoard().getId(),
                                    comment.getContent(),
                                    comment.getCreatedTime()
                            ))
                            .collect(Collectors.toList());

                    return new BoardDto(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getBoardType(),
                            getBoardTypeText(board.getBoardType()),
                            board.getImage(),
                            formattedCreatedTime,
                            userUid,
                            commentDtos
                    );
                })
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    public void deleteBoard(Long id) {
        if (!boardRepository.existsById(id)) {
            throw new RuntimeException("삭제하려는 게시글이 존재하지 않습니다.");
        }
        boardRepository.deleteById(id);
    }

    public void updateBoard(Long id, BoardDto boardDto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 기존 데이터를 업데이트
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setBoardType(boardDto.getBoardType());
        board.setImage(boardDto.getImage());
        boardRepository.save(board);
    }

    private String getBoardTypeText(Integer boardType) {
        if (boardType == null) return "기타"; // null 처리
        switch (boardType) {
            case 0: return "공지사항";
            case 1: return "QnA";
            case 2: return "후기";
            default: return "기타"; // 알 수 없는 값 처리
        }
    }
}
