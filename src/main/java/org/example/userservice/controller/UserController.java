package org.example.userservice.controller;

import org.antlr.v4.runtime.misc.Pair;
import org.example.userservice.dto.LoginRequestDto;
import org.example.userservice.dto.SignupRequestDto;
import org.example.userservice.dto.UserDto;
import org.example.userservice.dto.ValidateRequestDto;
import org.example.userservice.model.User;
import org.example.userservice.service.AuthService;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserDetails(@PathVariable Long id){
        User user = userService.getUserDetails(id);
        return getUserDto(user);
    }

    private UserDto getUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        return userDto;
    }
}
