package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;

import static org.hamcrest.Matchers.instanceOf;


import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class AuthServiceIT {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    private FarmService farmService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Tests that new user can be created sucessfuly by manager")
    void testSucessfulRegisterUser() throws Exception {
        //Given
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

        //System.out.println(currentUser.getRole().getName());
        //System.out.println(currentUser.getUsername());
        //System.out.println(currentUser.getId());

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //When
        ResponseEntity<?> result = authService.registerUser(signUpRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("User registered successfully!"));

        // Verify the user was saved in the repository
        User registeredUser = userRepository.findByUsername(signUpRequest.getUsername()).orElse(null);
        assertThat(registeredUser, notNullValue());
        assertThat(registeredUser.getEmail(), is(signUpRequest.getEmail()));
        assertThat(registeredUser.getUsername(), is(signUpRequest.getUsername()));
        assertThat(registeredUser.getRole().getName(), is(ERole.ROLE_FARM_MANAGER));
    }

    @Test
    @DisplayName("Tests that new user can not be created by manager if choosen username already exists")
    void testUsernameTakenSignup() throws Exception {
        //Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe");
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

        signUpRequest.setUsername(currentUser.getUsername());
        //When
        ResponseEntity<?> result = authService.registerUser(signUpRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Error: Username is already taken!"));
    }


    @Test
    @DisplayName("Tests that new user can not be created sucessfuly if current user has no authentication")
    void testNoAuthentication() throws Exception {
        //Given
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("123456789");
        signUpRequest.setRole("ROLE_FARM_MANAGER");

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(securityContext);

        //When
        ResponseEntity<?> result = authService.registerUser(signUpRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
    }

    @Test
    @DisplayName("Tests that new user and farm can be created sucessfuly")
    void testSucessfulRegisterUserAndFarm() throws Exception {
        //Given
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
        signUpFarmRequest.setFarmName("farm1");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Farm registered successfully!"));

        // Verify the user was saved in the repository
        User registeredFarmUser = userRepository.findByUsername(signUpFarmRequest.getUsername()).orElse(null);
        assertThat(registeredFarmUser, notNullValue());
        assertThat(registeredFarmUser.getEmail(), is(signUpFarmRequest.getEmail()));
        assertThat(registeredFarmUser.getUsername(), is(signUpFarmRequest.getUsername()));
        assertThat(registeredFarmUser.getRole().getName(), is(ERole.ROLE_FARM_OWNER));
        //Verify the farm was saved in the repository
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName());
        assertThat(farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()), is(true));
        //Verify code is used now
        assertThat(activationCode.getIsUsed(), is(true));
    }

    @Test
    @DisplayName("Tests that new user and farm can not be created when username is taken")
    void testUsernameTakenFarmSignup() throws Exception {
        //Given
        ActivationCode activationCode = entityManager.createQuery(
                        "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
                .setParameter("used", false)
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        User user1 = entityManager.find(User.class, 1);
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername(user1.getUsername());
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName("farm1");
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Error: Username is already taken!"));
    }

    @Test
    @DisplayName("Tests that new user and farm can not be created when farm name is taken")
    void testFarmNameTakenFarmSignup() throws Exception {
        //Given
        ActivationCode activationCode = entityManager.createQuery(
                        "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
                .setParameter("used", false)
                .setMaxResults(1)  // Ensures only one result is returned
                .getSingleResult();

        Farm farm = entityManager.find(Farm.class, 1);
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName(farm.getFarmName());
        signUpFarmRequest.setActivationCode(activationCode.getCode());

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Error: Farm Name is already taken!"));
    }

    @Test
    @DisplayName("Tests that new user and farm can not be created when activation code is empty")
    void testCodeEmptyFarmSignup() throws Exception {
        //Given
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName("farm1");
        signUpFarmRequest.setActivationCode("");

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Activation code does not exist."));
    }

    @Test
    @DisplayName("Tests that new user and farm can not be created when activation code is expired")
    void testCodeExpiredFarmSignup() throws Exception {
        //Given
        ActivationCode expiredCode = new ActivationCode();
        expiredCode.setCode("thisCodeIsExpired");
        expiredCode.setExpireDate(LocalDate.now().minusDays(1));
        expiredCode.setIsUsed(false);

        entityManager.persist(expiredCode);
        entityManager.flush();

        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName("farm1");
        signUpFarmRequest.setActivationCode(expiredCode.getCode());

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Activation code has expired."));
    }

    @Test
    @DisplayName("Tests that new user and farm can not be created when activation code is used")
    void testCodeUsedFarmSignup() throws Exception {
        //Given
        ActivationCode expiredCode = new ActivationCode();
        expiredCode.setCode("thisCodeIsUsed");
        expiredCode.setExpireDate(LocalDate.now().plusDays(1));
        expiredCode.setIsUsed(true);

        entityManager.persist(expiredCode);
        entityManager.flush();

        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest();
        signUpFarmRequest.setFirstName("John");
        signUpFarmRequest.setLastName("Doe");
        signUpFarmRequest.setUsername("newUser");
        signUpFarmRequest.setEmail("newuser@example.com");
        signUpFarmRequest.setPassword("password");
        signUpFarmRequest.setPhoneNumber("123456789");
        signUpFarmRequest.setFarmName("farm1");
        signUpFarmRequest.setActivationCode(expiredCode.getCode());

        //When
        ResponseEntity<?> result = authService.registerFarmAndFarmOwner(signUpFarmRequest);

        //Then
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody(), instanceOf(MessageResponse.class));
        MessageResponse responseBody = (MessageResponse) result.getBody();
        assertThat(responseBody.getMessage(), is("Activation code has already been used."));
    }

}
