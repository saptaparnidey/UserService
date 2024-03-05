package org.example.userservice.controller;

import org.example.userservice.dto.LoginRequestDto;
import org.example.userservice.dto.SignupRequestDto;
import org.example.userservice.dto.UserDto;
import org.example.userservice.model.User;
import org.example.userservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignupRequestDto signupRequestDto){
        User user = authService.signUp(signupRequestDto.getEmail(), signupRequestDto.getPassword());
        UserDto userDto = getUserDto(user);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try{
            User user = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            UserDto userDto = getUserDto(user);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }

    private UserDto getUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}