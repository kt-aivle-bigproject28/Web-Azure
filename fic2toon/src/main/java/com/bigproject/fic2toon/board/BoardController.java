package com.bigproject.fic2toon.board;

import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private ResourceLoader resourceLoader;
    private ShareServiceClient shareServiceClient;

    @Value("${spring.cloud.azure.storage.fileshare.share-name}")
    private String shareName;

    @GetMapping
    public String getBoardList(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId); // 사용자 타입 추가
        model.addAttribute("boardList", boardService.getBoardList()); // 게시판 목록 추가
        return "board/board"; // 게시판 뷰 반환
    }

    @GetMapping("/{id}")
    public String getBoardDetail(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId);
        model.addAttribute("board", boardService.getBoardById(id));
        return "board/detail";
    }

    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId); // 사용자 타입 추가
        model.addAttribute("board", new BoardDto());
        return "board/form";
    }

    @PostMapping("/form")
    public String saveForm(@ModelAttribute @Valid BoardDto boardDto,
                           @RequestParam("file") MultipartFile file,
                           HttpSession session,
                           Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");

        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);
        boardDto.setUserUid(loginUserId);

        if (!file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String directoryName = "data";

                ShareFileClient fileClient = shareServiceClient.getShareClient(shareName)
                        .getDirectoryClient(directoryName)
                        .getFileClient(fileName);

                fileClient.create(file.getSize());
                fileClient.upload(file.getInputStream(), file.getSize());

                // 파일 URL 설정
                String fileUrl = fileClient.getFileUrl();
                boardDto.setImage(fileUrl);

                System.out.println("File saved: " + fileUrl);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
                return "board/form";
            }
        }

        boardService.createBoard(boardDto);

        return "redirect:/board";
    }


    @PostMapping("/update/{id}")
    public String updateForm(@PathVariable Long id,
                             @ModelAttribute BoardDto boardDto,
                             @RequestParam MultipartFile file,
                             HttpSession session,
                             Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");

        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);

        if (!file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = "data/" + fileName;
                Resource resource = resourceLoader.getResource("azure-file://file-28/" + filePath);

                try (OutputStream os = ((WritableResource) resource).getOutputStream()) {
                    os.write(file.getBytes());
                }

                boardDto.setImage(filePath);

                System.out.println("File saved: " + boardDto.getImage());

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "파일 업로드에 실패했습니다.");
                return "board/form";
            }
        }

        boardService.updateBoard(id, boardDto);
        return "redirect:/board";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteForm(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        model.addAttribute("user", loginUserId);

        BoardDto board = boardService.getBoardById(id);

        // 권한 확인: 관리자 또는 작성자만 삭제 가능

        boardService.deleteBoard(id);

        // 삭제 후 게시글 목록으로 리다이렉트
        return "redirect:/board";
    }


    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser"); // 로그인한 사용자 ID를 가져옴

        if (loginUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUserId);

        BoardDto board = boardService.getBoardById(id);

        if (!loginUserId.equals(board.getUserUid())) {
            model.addAttribute("error", "수정 권한이 없습니다.");
            return "board/board";
        }

        model.addAttribute("board", board);
        return "board/update";
    }
}