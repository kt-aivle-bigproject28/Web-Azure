package com.bigproject.fic2toon.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupDto {
    @NotEmpty(message = "이름은 필수입니다.")
    private String name;

    @NotEmpty(message = "ID는 필수입니다.")
    private String uid;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    private String password;

    private String confirmPassword; // 비밀번호 확인 필드

    @NotEmpty(message = "전화번호는 필수입니다.")
    private String phone;

    @NotEmpty(message = "회사 코드는 필수입니다.")
    private String companyCode;

    @NotEmpty(message = "직위는 필수입니다.")
    private String position;
}