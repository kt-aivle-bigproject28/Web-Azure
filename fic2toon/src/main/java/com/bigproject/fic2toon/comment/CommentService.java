package com.bigproject.fic2toon.comment;

import com.bigproject.fic2toon.board.Board;
import com.bigproject.fic2toon.board.BoardRepository;
import com.bigproject.fic2toon.user.User;
import com.bigproject.fic2toon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public List<Comment> findByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }

    @Transactional
    public void saveComment(CommentDto commentDto) {
        Board board = boardRepository.findById(commentDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        User user = userRepository.findByUid(commentDto.getUserUid())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        Comment comment = Comment.builder()
                .content(commentDto.getContent())
                .user(user)
                .board(board)
                .createdTime(commentDto.getCreatedTime())
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userUid) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 댓글 작성자와 현재 로그인한 사용자가 같은지 확인
        if (!comment.getUser().getUid().equals(userUid)) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}