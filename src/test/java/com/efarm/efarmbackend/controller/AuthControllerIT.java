package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.LoginRequest;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class AuthControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Test successful signin of a existing user with role operator")
    void testSigninInSucessfulOperator() throws Exception {
        // Given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 1);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(usernameTest);
        loginRequest.setPassword(passwordTest);

        // When
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(usernameTest))
                .andExpect(jsonPath("$.email").value(emailTest))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_FARM_EQUIPMENT_OPERATOR"));
    }

    @Test
    @DisplayName("Test successful signin of a existing user with role manager")
    void testSigninInSucessfulManager() throws Exception {
        // Given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(usernameTest);
        loginRequest.setPassword(passwordTest);

        // When
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(usernameTest))
                .andExpect(jsonPath("$.email").value(emailTest))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_FARM_MANAGER"));
    }


    @Test
    @DisplayName("Test bad credentials of signin")
    void testBadCredentialsSignIn() throws Exception {
        // Given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 1);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);
        entityManager.merge(testUser);
        entityManager.flush();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(usernameTest);
        loginRequest.setPassword("upsieBadPassword");

        // When
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test for correct signout")
    void testGoodSignout() throws Exception {
        // Given
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        mockMvc.perform(post("/api/auth/signout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "jwtToken=; Path=/api"))
                .andExpect(content().json("{\"message\":\"You've been signed out!\"}"));


    }


    @Test
    @DisplayName("Test successful registration of a new user by manager")
    void testUserRegistrationByManager() throws Exception {
        // Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe");
        signUpRequest.setUsername("newUser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("123456789");
        signUpRequest.setRole("ROLE_FARM_MANAGER");

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
                .setParameter("roleName", ERole.ROLE_FARM_MANAGER)
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User registered successfully!\"}"));
    }

    @Test
    @DisplayName("Tests user registration by operator")
    void testUserRegistrationByOperator() throws Exception {
        // Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe");
        signUpRequest.setUsername("newUser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("123456789");
        signUpRequest.setRole("ROLE_FARM_MANAGER");

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
                .setParameter("roleName", ERole.ROLE_FARM_EQUIPMENT_OPERATOR)
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tests sucessful user and farm registration")
    void testUserFarmRegistration() throws Exception {
        // Given
        ActivationCode activationCode = entityManager.createQuery(
                        "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
                .setParameter("used", false)
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName("farmName");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Farm registered successfully!\"}"));
    }


}
