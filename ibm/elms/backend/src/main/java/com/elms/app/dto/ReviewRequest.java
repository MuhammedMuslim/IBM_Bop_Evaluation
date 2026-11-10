package com.elms.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank
    private String action;   // APPROVE or REJECT
    private String comment;
}
