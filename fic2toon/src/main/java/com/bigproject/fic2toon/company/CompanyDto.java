package com.bigproject.fic2toon.company;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private Long id;
    private String code;
    private String name;

    public CompanyDto(Company company) {
        this.id = company.getId();
        this.code = company.getCode();
        this.name = company.getName();
    }
}