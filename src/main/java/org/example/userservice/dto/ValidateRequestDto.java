package org.example.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidateRequestDto {

    String token;
    Long userId;
}
