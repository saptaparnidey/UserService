package org.example.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendEmailMessageDto {
    private String to;
    private String from;
    private String subject;
    private String body;
}
