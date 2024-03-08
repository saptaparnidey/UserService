package org.example.userservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.example.userservice.model.Session;
import org.example.userservice.model.SessionStatus;
import org.example.userservice.model.User;
import org.example.userservice.repository.SessionRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    SessionRepository sessionRepository;

    public User signUp(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            User user = new User();
            user.setEmail(email);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            User savedUser = userRepository.save(user);
            return savedUser;
        }
        return userOptional.get();
    }

    public Pair<User, MultiValueMap<String, String>> login(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            return null;
        }

        User user = userOptional.get();
//        if(!user.getPassword().equals(password)){
//            return null;
//        }
        if(!bCryptPasswordEncoder.matches(password,user.getPassword())){
            return null;
        }


        //Token Generation
//        String message = "{\n" +
//                "   \"email\": \"tobby@scaler.com\",\n" +
//                "   \"roles\": [\n" +
//                "      \"learner\",\n" +
//                "      \"buddy\"\n" +
//                "   ],\n" +
//                "   \"expirationDate\": \"2ndApril2024\"\n" +
//                "}";
//
//        byte[] content = message.getBytes(StandardCharsets.UTF_8);
//        String token = Jwts.builder().content(content).signWith(secret).compact();

        Map<String, Object> jwtData = new HashMap<>();
        jwtData.put("email", user.getEmail());
        jwtData.put("roles", user.getRoles());
        long nowInMillis = System.currentTimeMillis();
        jwtData.put("expiry", new Date(nowInMillis + 10000));
        jwtData.put("createdAt", new Date(nowInMillis));


        MacAlgorithm algorithm = Jwts.SIG.HS256;
        SecretKey secret = algorithm.key().build();
        String token = Jwts.builder().claims(jwtData).signWith(secret).compact();

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setUser(user);
        session.setToken(token);
        session.setExpiryAt(new Date(nowInMillis+10000));
        sessionRepository.save(session);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE, token);


        return new Pair<User, MultiValueMap<String,String>>(user, headers);
    }
}
