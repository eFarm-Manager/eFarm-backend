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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.auth.ChangePasswordRequest;
import com.efarm.efarmbackend.payload.request.auth.LoginRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeByLoggedOwnerRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @BeforeEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    /*
     * Post /signin
     */
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
        mockMvc.perform(post("/auth/signin")
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
        mockMvc.perform(post("/auth/signin")
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
        mockMvc.perform(post("/auth/signin")
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
            mockMvc.perform(post("/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // When
        mockMvc.perform(post("/auth/signin")
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
        mockMvc.perform(post("/auth/signout"))
        // Then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "jwtToken=; Path=/api; Secure; SameSite=None"))
                .andExpect(jsonPath("$.message").value("Wylogowano"));
    }
    /*
     * POST /signup
     */

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
                .setMaxResults(1)  
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zarejestrowano nowego użytkownika"));

        User newUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", signUpRequest.getUsername())
                .getSingleResult();
        
        assertThat(newUser.getRole().getName(), is(ERole.ROLE_FARM_MANAGER));
        assertThat(newUser.getFirstName(), is(signUpRequest.getFirstName()));
        assertThat(newUser.getLastName(), is(signUpRequest.getLastName()));
        assertThat(newUser.getEmail(), is(signUpRequest.getEmail()));
        assertThat(newUser.getPhoneNumber(),is(signUpRequest.getPhoneNumber()));
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
                .setMaxResults(1)  
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isForbidden());
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
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Podana nazwa użytkownika jest już zajęta"));

    }
    /*
     * POST /signupfarm
     */
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
        mockMvc.perform(post("/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zarejestrowano nową farmę"));

        User newUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", signUpFarmRequest.getUsername())
                .getSingleResult();
        ActivationCode usedActivationCode = entityManager.createQuery(
                        "SELECT a FROM ActivationCode a WHERE a.code = :code", ActivationCode.class)
                .setParameter("code", activationCode.getCode())
                .getSingleResult();
        
        assertThat(newUser.getFirstName(), is(signUpFarmRequest.getFirstName()));
        assertThat(newUser.getLastName(), is(signUpFarmRequest.getLastName()));
        assertThat(newUser.getEmail(), is(signUpFarmRequest.getEmail()));
        assertThat(newUser.getPhoneNumber(), is(signUpFarmRequest.getPhoneNumber()));
        assertThat(newUser.getFarm().getFarmName(), is(signUpFarmRequest.getFarmName()));
        assertThat(newUser.getFarm().getIdActivationCode(), is(usedActivationCode.getId()));
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
        mockMvc.perform(post("/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrana nazwa użytkownika jest już zajęta"));

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
        mockMvc.perform(post("/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrana nazwa farmy jest już zajęta"));

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
        mockMvc.perform(post("/auth/signupfarm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpFarmRequest)))
        // Then
                .andExpect(status().isBadRequest())                
                .andExpect(jsonPath("$.message").value("Podany kod aktywacyjny nie istnieje"));

    }
    /*
     * POST /update-activation-code
     */

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
        mockMvc.perform(post("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowano kod aktywacyjny"));

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
        mockMvc.perform(post("/auth/update-activation-code")
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
        mockMvc.perform(post("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Brak uprawnień"));

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
        mockMvc.perform(post("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Podany kod aktywacyjny nie istnieje"));

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
            mockMvc.perform(post("/auth/update-activation-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
                    .andExpect(status().isBadRequest());
        }

        // When
        mockMvc.perform(post("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Zbyt wiele nieudanych prób. Spróbuj ponownie później."));
    }
    /*
     * PUT /update-activation-code
     */

    @Test
    @DisplayName("Test updating activation code by logged in owner")
    void testUpdateActivationCodeByLoggedOwner() throws Exception {
        //given
        Role role = entityManager.find(Role.class, 3);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ActivationCode activationCode = entityManager.find(ActivationCode.class, 5);

        UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest = new UpdateActivationCodeByLoggedOwnerRequest();
        updateActivationCodeByLoggedOwnerRequest.setPassword("password123");
        updateActivationCodeByLoggedOwnerRequest.setNewActivationCode(activationCode.getCode());

        //when
        mockMvc.perform(put("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeByLoggedOwnerRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowano kod aktywacyjny"));
    }

    @Test
    @DisplayName("Test updating activation code with invalid password")
    void testUpdateActivationCodeWithInvalidPassword() throws Exception {
        //given
        Role role = entityManager.find(Role.class, 3);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest = new UpdateActivationCodeByLoggedOwnerRequest();
        updateActivationCodeByLoggedOwnerRequest.setPassword("wrongPassword"); // Incorrect password
        updateActivationCodeByLoggedOwnerRequest.setNewActivationCode("doesntMatterHere");

        //when
        mockMvc.perform(put("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeByLoggedOwnerRequest)))
        // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Nieprawidłowe hasło"));
    }

    @Test
    @DisplayName("Test updating activation code with the invalid code for any reason here used")
    void testUpdateActivationCodeWithInvalidCode() throws Exception {
        //given
        Role role = entityManager.find(Role.class, 3);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ActivationCode activationCode = entityManager.find(ActivationCode.class, 13);

        UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest = new UpdateActivationCodeByLoggedOwnerRequest();
        updateActivationCodeByLoggedOwnerRequest.setPassword("password123");
        updateActivationCodeByLoggedOwnerRequest.setNewActivationCode(activationCode.getCode());

        //when
        mockMvc.perform(put("/auth/update-activation-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActivationCodeByLoggedOwnerRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Podany kod aktywacyjny został już wykorzystany"));
    }
    /*
     * PUT /change-password
     */

    @Test
    @DisplayName("Test updating updating password by logged in user")
    void testUpdatePassword() throws Exception {
        //given
        Role role = entityManager.find(Role.class, 1);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("password123");
        changePasswordRequest.setNewPassword("321drowssap");

        //when
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hasło zostało pomyślnie zmienione"));
    }

    @Test
    @DisplayName("Test updating password with invalid current password")
    void testUpdatePasswordWithInvalidCurrentPassword() throws Exception {
        // Given
        Role role = entityManager.find(Role.class, 1);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("wrongPassword"); // Invalid password
        changePasswordRequest.setNewPassword("321drowssap");

        // When
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
        // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Podano nieprawidłowe aktualne hasło"));
    }

    @Test
    @DisplayName("Test updating password with new password same as current")
    void testUpdatePasswordWithSameCurrentAndNewPassword() throws Exception {
        // Given
        Role role = entityManager.find(Role.class, 1);
        Farm farm = entityManager.find(Farm.class, 5);
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("email@gmail.com");
        newUser.setPassword(passwordEncoder.encode("password123"));
        newUser.setRole(role);
        newUser.setPhoneNumber("123465798");
        newUser.setFarm(farm);
        newUser.setIsActive(true);
        entityManager.merge(newUser);
        entityManager.flush();

        User currentUser = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "username")
                .setMaxResults(1)
                .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("password123");
        changePasswordRequest.setNewPassword("password123"); // Same as current password

        // When
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
        // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nowe hasło nie może być takie samo jak poprzednie"));
    }
}
