package com.codepost.CodePost.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeEntityDTO {

    private String cName;
    private Double version;

    public CodeEntityDTO(String cName, Double version) {
        this.cName = cName;
        this.version = version;
    }

}
