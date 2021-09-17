package com.reborn.golf.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NoticeDto {

    private Long num;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Integer views;

    private String email;

    private String name;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
