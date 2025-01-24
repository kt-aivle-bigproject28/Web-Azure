package com.bigproject.fic2toon.comment;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public String writeComment(@ModelAttribute @Valid CommentDto commentDto,
                               BindingResult bindingResult,
                               @RequestParam Long boardId,
                               HttpSession session,
                               Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId); // 사용자 타입 추가
        commentDto.setUserUid(loginUserId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "댓글 작성 중 오류가 발생했습니다.");
            return "redirect:/board/" + boardId; // 오류 발생 시 게시글 상세 페이지로 리다이렉트
        }

        commentDto.setUserUid(loginUserId);
        commentDto.setBoardId(boardId);

        commentService.saveComment(commentDto);

        return "redirect:/board/" + boardId; // 댓글 작성 후 게시글 상세 페이지로 리다이렉트
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                HttpSession session,
                                Model model,
                                @RequestParam Long boardId) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId);

        commentService.deleteComment(commentId, loginUserId);

        return "redirect:/board/" + boardId;
    }
}
