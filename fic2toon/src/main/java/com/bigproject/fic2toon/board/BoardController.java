package com.bigproject.fic2toon.board;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @PostConstruct
    public void initializeBlobContainer() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
            System.out.println("Blob container '" + containerName + "' created successfully.");
        } else {
            System.out.println("Blob container '" + containerName + "' already exists.");
        }
    }

    @GetMapping
    public String getBoardList(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        model.addAttribute("boardList", boardService.getBoardList());
        return "board/board";
    }

    @GetMapping("/{id}")
    public String getBoardDetail(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
        model.addAttribute("board", boardService.getBoardById(id));
        return "board/detail";
    }

    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);
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
                BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
                        .getBlobClient(fileName);

                blobClient.upload(file.getInputStream(), file.getSize(), true);
                String fileUrl = blobClient.getBlobUrl();
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
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             HttpSession session,
                             Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
                        .getBlobClient(fileName);

                blobClient.upload(file.getInputStream(), file.getSize(), true);
                String fileUrl = blobClient.getBlobUrl();
                boardDto.setImage(fileUrl);
                System.out.println("File updated: " + fileUrl);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
                return "board/form";
            }
        }
        boardService.updateBoard(id, boardDto);
        return "redirect:/board";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteForm(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUserId);

        BoardDto board = boardService.getBoardById(id);

        if (board.getImage() != null && !board.getImage().isEmpty()) {
            try {
                String blobName = board.getImage().substring(board.getImage().lastIndexOf('/') + 1);
                BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
                        .getBlobClient(blobName);

                blobClient.delete();
                System.out.println("File deleted: " + blobName);
            } catch (Exception e) {
                e.printStackTrace();
                // 파일 삭제 실패 처리
            }
        }

        boardService.deleteBoard(id);
        return "redirect:/board";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        String loginUserId = (String) session.getAttribute("loginUser");
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
