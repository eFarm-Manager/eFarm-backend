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
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
        testUser.setPhoneNumber("");
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
                        .content(objectMapper.writeValueAsString(loginRequest)))
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
        testUser.setPhoneNumber("123465798");
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
                        .content(objectMapper.writeValueAsString(loginRequest)))
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
                        .content(objectMapper.writeValueAsString(loginRequest)))
        // Then
                .andExpect(status().isUnauthorized())                
                .andExpect(jsonPath("$.message").value("Nieprawidłowe dane logowania"));

    }

    @Test
    @DisplayName("Test sign in too many attempts")
    void testSigninInTooManyAttempts() throws Exception {
        // Given
        String usernameTest = "user2";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode("differentPass"));
        testUser.setRole(role);
        testUser.setPhoneNumber("123465798");
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(usernameTest);
        loginRequest.setPassword(passwordTest);

        for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isUnauthorized());
            }

        // When
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
        // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Zbyt wiele nieudanych prób logowania. Spróbuj ponownie później."));
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

        //When
        mockMvc.perform(post("/api/auth/signout"))
        // Then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "jwtToken=; Path=/api"))
                .andExpect(content().json("{\"message\":\"You've been signed out!\"}"));


    }

    //Singup User

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
        signUpRequest.setPhoneNumber("");
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
                        .content(objectMapper.writeValueAsString(signUpRequest)))
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
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test registration failure due to validation errors")
    void testUserRegistrationValidationErrors() throws Exception {
        // Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("");  // Empty fields to trigger validation errors
        signUpRequest.setLastName("");
        signUpRequest.setUsername("newUser");
        signUpRequest.setEmail("invalid-email");  // Invalid email format
        signUpRequest.setPassword("pass");  // Too short to meet validation
        signUpRequest.setPhoneNumber("");
        signUpRequest.setRole("ROLE_FARM_MANAGER");
    
        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
                .setParameter("roleName", ERole.ROLE_FARM_MANAGER)
                .setMaxResults(1)
                .getSingleResult();
    
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    
        // When
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("password: size must be between 6 and 40")))
                .andExpect(content().string(containsString("email: must be a well-formed email address")))
                .andExpect(content().string(containsString("firstName: size must be between 3 and 30")))
                .andExpect(content().string(containsString("lastName: size must be between 3 and 40")));
    
        }
    
    @Test
    @DisplayName("Test registration failure when username is already taken")
    void testUserRegistrationUsernameTaken() throws Exception {
        // Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe"); 
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("");
        signUpRequest.setRole("ROLE_FARM_MANAGER");    
        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
                .setParameter("roleName", ERole.ROLE_FARM_MANAGER)
                .setMaxResults(1)
                .getSingleResult();    
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
                
        signUpRequest.setUsername(userDetails.getUsername()); 
        // When
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\":\"Error: Username is already taken!\"}"));
    }

    

    // Signup farm
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
        signUpFarmRequest.setPhoneNumber("");
        signUpFarmRequest.setFarmName("farmName");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Farm registered successfully!\"}"));
    }

    @Test
    @DisplayName("Test registration fails due to validation errors")
    void testUserFarmRegistrationWithErrors() throws Exception {
        // Given
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName(""); // Invalid: empty first name
        signUpFarmRequest.setLastName("");  // Invalid: empty last name
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("invalid-email"); // Invalid: wrong email format
        signUpFarmRequest.setPassword("short"); // Invalid: too short
        signUpFarmRequest.setFarmName("farmName");
        signUpFarmRequest.setActivationCode("someActivationCode"); 
        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("password: size must be between 6 and 40")))
                .andExpect(content().string(containsString("email: must be a well-formed email address")))
                .andExpect(content().string(containsString("firstName: size must be between 3 and 30")))
                .andExpect(content().string(containsString("lastName: size must be between 3 and 40")));
    }
    @Test
    @DisplayName("Test registration fails due to taken username")
    void testUserFarmRegistrationWithTakenUsername() throws Exception {
        // Given
        ActivationCode activationCode = entityManager.createQuery(
                "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
        .setParameter("used", false)
        .setMaxResults(1)  // Ensures only one result is returned
        .getSingleResult();

        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("");
        signUpFarmRequest.setFarmName("farmName");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        User user = entityManager.find(User.class, 1);
        signUpFarmRequest.setUsername(user.getUsername()); 
    
        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error: Username is already taken!")));
    }
    @Test
    @DisplayName("Test registration fails due to taken farm name")
    void testUserFarmRegistrationWithTakenFarmName() throws Exception {
        // Given
        ActivationCode activationCode = entityManager.createQuery(
                "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
        .setParameter("used", false)
        .setMaxResults(1)  // Ensures only one result is returned
        .getSingleResult();

        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("");
        signUpFarmRequest.setUsername("uniqueUserName");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        Farm farm = entityManager.find(Farm.class, 1);
        signUpFarmRequest.setFarmName(farm.getFarmName()); 
        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error: Farm Name is already taken!")));
    }

    @Test
    @DisplayName("Test registration fails due to invalid activation code")
    void testUserFarmRegistrationWithInvalidActivationCode() throws Exception {
        // Given
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("");
        signUpFarmRequest.setFarmName("farmName");
        signUpFarmRequest.setActivationCode("invalidActivationCode"); 
    
        // When
        mockMvc.perform(post("/api/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Activation code does not exist.")));
    }

        // updateActivationCode

    @Test
    @DisplayName("Test updating activation code by owner")
    void testUpdateActivationCodeByOwner() throws Exception {
        // given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 3);
        Farm farm = entityManager.find(Farm.class, 5);
        ActivationCode activationCode = entityManager.createQuery(
                "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
        .setParameter("used", false)
        .setMaxResults(1)  // Ensures only one result is returned
        .getSingleResult();

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setPhoneNumber("");
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();

        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest();
        updateActivationCodeRequest.setUsername(usernameTest);
        updateActivationCodeRequest.setPassword(passwordTest);
        updateActivationCodeRequest.setNewActivationCode(activationCode.getCode());

        //when
        mockMvc.perform(post("/api/auth/update-activation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Activation code updated successfully for the farm.\"}"));
    }

    @Test
    @DisplayName("Test updating activation code fails due to invalid credentials")
    void testUpdateActivationCodeWithInvalidCredentials() throws Exception {
        // Given
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest();
        updateActivationCodeRequest.setUsername("invalidUser");
        updateActivationCodeRequest.setPassword("wrongPassword");
        updateActivationCodeRequest.setNewActivationCode("someActivationCode");
    
        // When
        mockMvc.perform(post("/api/auth/update-activation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Test updating activation code fails due to insufficient permissions")
    void testUpdateActivationCodeWithoutPermissions() throws Exception {
        // Given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);
        ActivationCode activationCode = entityManager.createQuery(
                "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
        .setParameter("used", false)
        .setMaxResults(1)  // Ensures only one result is returned
        .getSingleResult();

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setPhoneNumber("");
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();
    
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest();
        updateActivationCodeRequest.setUsername(usernameTest);
        updateActivationCodeRequest.setPassword(passwordTest);
        updateActivationCodeRequest.setNewActivationCode(activationCode.getCode());
    
        // When
        mockMvc.perform(post("/api/auth/update-activation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\":\"Brak uprawnień\"}"));
    }

    @Test
    @DisplayName("Test updating activation code fails due to invalid activation code")
    void testUpdateActivationCodeWithInvalidActivationCode() throws Exception {
        // Given
        String usernameTest = "user1";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 3);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode(passwordTest));
        testUser.setPhoneNumber("");
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();
    
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest();
        updateActivationCodeRequest.setUsername(usernameTest);
        updateActivationCodeRequest.setPassword(passwordTest);
        updateActivationCodeRequest.setNewActivationCode("invalidActivationCode");
    
        // When
        mockMvc.perform(post("/api/auth/update-activation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\":\"Activation code does not exist.\"}"));
    }

    @Test
    @DisplayName("Test code update too many attempts")
    void testCodeUpdateInTooManyAttempts() throws Exception {
        // Given
        String usernameTest = "user3";
        String passwordTest = "StrongPassword123";
        String emailTest = "john.doe@gmail.com";
        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);
        ActivationCode activationCode = entityManager.createQuery(
                "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
        .setParameter("used", false)
        .setMaxResults(1)  // Ensures only one result is returned
        .getSingleResult();

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail(emailTest);
        testUser.setPassword(passwordEncoder.encode("differentPass"));
        testUser.setRole(role);
        testUser.setPhoneNumber("123465798");
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.merge(testUser);
        entityManager.flush();

        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest();
        updateActivationCodeRequest.setUsername(usernameTest);
        updateActivationCodeRequest.setPassword(passwordTest);
        updateActivationCodeRequest.setNewActivationCode(activationCode.getCode());

        for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
                        .andExpect(status().isBadRequest());
            }

        // When
        mockMvc.perform(post("/api/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Zbyt wiele nieudanych prób. Spróbuj ponownie później."));
    }
}
