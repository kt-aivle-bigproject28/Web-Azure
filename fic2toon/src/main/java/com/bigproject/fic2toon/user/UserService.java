package com.bigproject.fic2toon.user;

import com.bigproject.fic2toon.company.Company;
import com.bigproject.fic2toon.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok을 사용하여 생성자 자동 생성
public class UserService {

    private final UserRepository userRepository; // 사용자 정보를 저장할 리포지토리
    private final CompanyRepository companyRepository; // 회사 정보를 저장할 리포지토리
    private final PasswordEncoder passwordEncoder; // 비밀번호 인코더

    public Optional<User> findByUid(String uid) {
        return userRepository.findByUid(uid); // uid로 사용자를 조회하는 repository 메서드 호출
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }


    @Transactional // 데이터베이스 트랜잭션 관리
    public void createUser(SignupDto signupDto, Model model) {
        try {
            // 회사 코드로 회사 조회
            Optional<Company> optionalCompany = companyRepository.findByCode(signupDto.getCompanyCode());

            if (optionalCompany.isEmpty()) {
                throw new IllegalArgumentException("유효하지 않은 회사 코드입니다."); // 예외 처리
            }

            Company company = optionalCompany.get();

            // UserDto를 User 엔티티로 변환
            User user = User.builder()
                    .name(signupDto.getName())
                    .uid(signupDto.getUid())
                    .password(encodePassword(signupDto.getPassword())) // 비밀번호 암호화
                    .phone(signupDto.getPhone())
                    .position(signupDto.getPosition())
                    .type(UserType.GUEST)
                    .company(company) // 회사 설정
                    .build();

            // 사용자 정보 저장
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage()); // 에러 메시지를 모델에 추가
            throw e; // 예외를 다시 던져서 호출자에게 알림
        } catch (Exception e) {
            model.addAttribute("errorMessage", "사용자 생성 중 오류가 발생했습니다."); // 일반 오류 메시지 추가
            throw e; // 예외를 다시 던져서 호출자에게 알림
        }
    }

    public String authenticate(LoginRequestDto loginRequest) {
        Optional<User> optionalUser = userRepository.findByUid(loginRequest.getUid());

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        User user = optionalUser.get();

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공 시 토큰 생성 (여기서는 간단히 사용자 ID를 반환)
        return generateToken(user); // JWT 토큰 생성 로직 추가 가능
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password); // PasswordEncoder를 사용하여 비밀번호 암호화
    }

    private String generateToken(User user) {
        // JWT 토큰 생성 로직 (생략)
        return "dummy-token"; // 실제 JWT 토큰으로 대체해야 함
    }

    public boolean isUidAvailable(String uid) {
        return userRepository.findByUid(uid).isEmpty();
    }
}
