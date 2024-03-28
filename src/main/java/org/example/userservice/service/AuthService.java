package org.example.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.example.userservice.client.KafkaProducerClient;
import org.example.userservice.dto.SendEmailMessageDto;
import org.example.userservice.dto.UserDto;
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

    @Autowired
    private SecretKey secret;

    @Autowired
    private KafkaProducerClient kafkaProducerClient;

    @Autowired
    ObjectMapper objectMapper;

    public User signUp(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            User user = new User();
            user.setEmail(email);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            User savedUser = userRepository.save(user);
            return savedUser;
        }

//        UserDto userDto = new UserDto();
//        userDto.setEmail(email);
        //Put message in a Topic
        try {
            SendEmailMessageDto sendEmailMessageDto = new SendEmailMessageDto();
            sendEmailMessageDto.setTo(email);
            sendEmailMessageDto.setFrom("admin@scaler.com");
            sendEmailMessageDto.setSubject("Welcome to Scaler!");
            sendEmailMessageDto.setBody("Have a Good Learning Experience...");
            kafkaProducerClient.sendMessage("sendEmail", objectMapper.writeValueAsString(sendEmailMessageDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
        jwtData.put("expiry", new Date(nowInMillis + 100000000));
        jwtData.put("createdAt", new Date(nowInMillis));

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

    public Boolean validateToken(String token, Long userId){
        Optional<Session> optionalSession = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (optionalSession.isEmpty()){
            System.out.println("No Token or User Found");
            return false;
        }

        Session session = optionalSession.get();

        String storedToken = session.getToken();

        JwtParser jwtParser = Jwts.parser().verifyWith(secret).build();
        Claims claims = jwtParser.parseSignedClaims(storedToken).getPayload();

        long nowInMillis = System.currentTimeMillis();
        long tokenExpiry = (Long)claims.get("expiry");

        if(nowInMillis > tokenExpiry) {
            System.out.println(nowInMillis);
            System.out.println(tokenExpiry);
            System.out.println("Token has expired");
            return false;
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()){
            return false;
        }

        String email = optionalUser.get().getEmail();
        if(!email.equals(claims.get("email"))) {
            System.out.println(email);
            System.out.println(claims.get("email"));
            System.out.println(claims.get("User doesn't match"));
            return false;
        }

        return true;

    }
}
