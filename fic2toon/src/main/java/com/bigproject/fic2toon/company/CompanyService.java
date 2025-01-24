package com.bigproject.fic2toon.company;

import com.bigproject.fic2toon.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public Company findByCode(String code) {
        return companyRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회사입니다."));
    }

    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }
}
