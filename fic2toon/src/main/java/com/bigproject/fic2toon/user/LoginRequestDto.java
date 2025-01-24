package com.bigproject.fic2toon.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String uid; // 사용자 ID
    private String password; // 비밀번호
}