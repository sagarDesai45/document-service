package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private String id;
    @NotBlank(message = "title should not be blank")
    private String title;
    @NotBlank(message = "content should not be blank")
    private String content;
    private String tenantId;
}
