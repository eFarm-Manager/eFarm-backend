package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.*;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupRequest;
import com.efarm.efarmbackend.repository.user.RoleRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User createFarmOwner(SignupFarmRequest signUpFarmRequest) {
        User user = new User(
                signUpFarmRequest.getFirstName(),
                signUpFarmRequest.getLastName(),
                signUpFarmRequest.getUsername(),
                signUpFarmRequest.getEmail(),
                encoder.encode(signUpFarmRequest.getPassword()),
                signUpFarmRequest.getPhoneNumber()
        );
        Role managerRole = roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_OWNER is not found."));

        user.setRole(managerRole);
        return user;
    }

    public User createFarmUser(SignupRequest signUpRequest) {
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPhoneNumber());

        Role assignedRole = assignUserRole(signUpRequest.getRole());
        user.setRole(assignedRole);
        return user;
    }

    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.error("Authentication object is null. No user is authenticated.");
            throw new RuntimeException("No authentication found. User may not be logged in.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl currentUserDetails) {
            return userRepository.findById(Long.valueOf(currentUserDetails.getId()))
                    .orElseThrow(() -> new RuntimeException("Error: Current user not found."));
        } else {
            throw new RuntimeException("Error: Unexpected principal type.");
        }
    }

    public Farm getLoggedUserFarm() {
        return getLoggedUser().getFarm();
    }

    public Farm getUserFarmById(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User with ID " + userId + " not found."));
        return currentUser.getFarm();
    }

    public List<String> getLoggedUserRoles(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public Boolean isPasswordValidForLoggedUser(String providedPassword) {
        return encoder.matches(providedPassword, getLoggedUser().getPassword());
    }

    public void updatePasswordForLoggedUser(String newPassword) {
        User loggedUser = getLoggedUser();
        loggedUser.setPassword(encoder.encode(newPassword));
        userRepository.save(loggedUser);
    }

    public List<User> getAllOwnersForFarm(Integer farmId) {
        return userRepository.findOwnersForFarm(farmId);
    }

    public Optional<User> getActiveUserById(UserDetailsImpl userDetails) throws RuntimeException {
        Optional<User> loggingUser = userRepository.findById(Long.valueOf(userDetails.getId()));
        if (!loggingUser.isPresent() || !loggingUser.get().getIsActive()) {
            throw new RuntimeException("Użytkownik jest nieaktywny!");
        }
        return loggingUser;
    }

    public List<UserDTO> getFarmUsersByFarmId() {
        Farm loggedUserFarm = getLoggedUserFarm();
        List<User> users = getUsersByFarmId(loggedUserFarm.getId());
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public List<UserSummaryDTO> getActiveFarmUsersByFarmId() {
        Farm loggedUserFarm = getLoggedUserFarm();
        List<User> users = getActiveUsersByFarmId(loggedUserFarm.getId());
        return users.stream()
                .map(UserSummaryDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllUsersForFarm(Farm farm) {
        List<User> users = getUsersByFarmId(farm.getId());
        userRepository.deleteAll(users);
    }

    private List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }

    private List<User> getActiveUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmIdAndIsActive(farmId, true);
    }

    private Role assignUserRole(String strRole) {
        return switch (strRole) {
            case "ROLE_FARM_OWNER" -> roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_OWNER is not found."));
            case "ROLE_FARM_MANAGER" -> roleRepository.findByName(ERole.ROLE_FARM_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_MANAGER is not found."));
            default -> roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_EQUIPMENT_OPERATOR is not found."));
        };
    }
}

