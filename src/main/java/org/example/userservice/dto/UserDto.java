package org.example.userservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.userservice.model.Role;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDto {

    private String email;

//    private Set<Role> roles = new HashSet<>();
}
