package com.reborn.golf.security.dto;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;

@Log4j2
@Setter
@Getter
public class AuthMemeberDto extends User {

    private Integer idx;
    private boolean fromSocial;

    public AuthMemeberDto(Integer idx, String username, String password, boolean fromSocial, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.idx = idx;
        this.fromSocial = fromSocial;
    }

}
