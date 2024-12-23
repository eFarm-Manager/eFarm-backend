package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.*;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest;
import com.efarm.efarmbackend.payload.request.user.ChangeUserPasswordRequest;
import com.efarm.efarmbackend.payload.request.user.UpdateUserRequest;
import com.efarm.efarmbackend.repository.user.RoleRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl implements UserManagementService, UserAuthenticationService, UserNotificationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
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
                .orElseThrow(() -> new RuntimeException("Nie znaleziono roli: " + ERole.ROLE_FARM_OWNER));

        user.setRole(managerRole);
        return user;
    }

    @Override
    public User createFarmUser(SignupUserRequest signUpUserRequest) {
        User user = new User(
                signUpUserRequest.getFirstName(),
                signUpUserRequest.getLastName(),
                signUpUserRequest.getUsername(),
                signUpUserRequest.getEmail(),
                encoder.encode(signUpUserRequest.getPassword()),
                signUpUserRequest.getPhoneNumber());

        Role assignedRole = assignUserRole(signUpUserRequest.getRole());
        user.setRole(assignedRole);
        return user;
    }

    @Override
    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.error("Authentication object is null. No user is authenticated.");
            throw new RuntimeException("Nie znaleziono uwierzytelnienia. Prawdopodobnie nie jesteś zalogowany.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl currentUserDetails) {
            return userRepository.findById(Long.valueOf(currentUserDetails.getId()))
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        } else {
            throw new RuntimeException("Nieoczekiwany typ podmiotu zabezpieczeń");
        }
    }

    @Override
    public Farm getLoggedUserFarm() {
        return getLoggedUser().getFarm();
    }

    @Override
    public Farm getUserFarmById(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik o id: " + userId + " nie został znaleziony"));
        return currentUser.getFarm();
    }

    @Override
    public List<String> getLoggedUserRoles(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isPasswordValidForLoggedUser(String providedPassword) {
        return encoder.matches(providedPassword, getLoggedUser().getPassword());
    }

    @Override
    public void updatePasswordForLoggedUser(String newPassword) {
        User loggedUser = getLoggedUser();
        loggedUser.setPassword(encoder.encode(newPassword));
        userRepository.save(loggedUser);
    }

    @Override
    public List<User> getAllOwnersForFarm(Integer farmId) {
        return userRepository.findOwnersForFarm(farmId);
    }

    @Override
    public Optional<User> getActiveUserById(UserDetailsImpl userDetails) throws RuntimeException {
        Optional<User> loggingUser = userRepository.findById(Long.valueOf(userDetails.getId()));
        if (!loggingUser.isPresent() || !loggingUser.get().getIsActive()) {
            throw new RuntimeException("Użytkownik jest nieaktywny");
        }
        return loggingUser;
    }

    @Override
    public List<UserDTO> getFarmUsersByFarmId() {
        Farm loggedUserFarm = getLoggedUserFarm();
        List<User> users = getUsersByFarmId(loggedUserFarm.getId());
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryDTO> getActiveFarmUsersByFarmId() {
        Farm loggedUserFarm = getLoggedUserFarm();
        List<User> users = getActiveUsersByFarmId(loggedUserFarm.getId());
        return users.stream()
                .map(UserSummaryDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllUsersForFarm(Farm farm) {
        List<User> users = getUsersByFarmId(farm.getId());
        userRepository.deleteAll(users);
    }

    @Override
    @Transactional
    public void toggleUserActiveStatus(Integer userId) {
        User loggedUser = getLoggedUser();
        User userToToggle = userRepository.findByIdAndFarmId(userId, loggedUser.getFarm().getId())
                .orElseThrow(() -> new RuntimeException("Wybrany użytkownik nie istnieje"));
        if (loggedUser.getRole().getName().equals(ERole.ROLE_FARM_MANAGER) &&
                userToToggle.getRole().getName().equals(ERole.ROLE_FARM_OWNER)) {
            throw new RuntimeException("Nie możesz zmienić statusu aktywności właściciela gospodarstwa");
        }
        if (loggedUser.equals(userToToggle)) {
            throw new RuntimeException("Nie możesz deaktywować siebie");
        }
        userToToggle.setIsActive(!userToToggle.getIsActive());
        userRepository.save(userToToggle);
    }

    @Override
    @Transactional
    public void updateUserDetails(Integer userId, UpdateUserRequest updateUserRequest) {
        Farm loggedUserFarm = getLoggedUserFarm();
        User user = userRepository.findByIdAndFarmId(userId, loggedUserFarm.getId())
                .orElseThrow(() -> new RuntimeException("Wybrany użytkownik nie istnieje"));

        updateUserProperties(user, updateUserRequest);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserPassword(Integer userId, ChangeUserPasswordRequest updatePasswordRequest) {
        Farm loggedUserFarm = getLoggedUserFarm();
        User user = userRepository.findByIdAndFarmId(userId, loggedUserFarm.getId())
                .orElseThrow(() -> new RuntimeException("Wybrany użytkownik nie istnieje"));

        user.setPassword(encoder.encode(updatePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<User> filterOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators) {
        return activeFarmOperators.stream()
                .filter(operator -> operatorIds.contains(operator.getId()))
                .toList();
    }

    @Override
    public List<User> filterInvalidOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators) {
        return userRepository.findAllById(operatorIds.stream().map(Integer::longValue).toList()).stream()
                .filter(user -> !activeFarmOperators.contains(user))
                .toList();
    }

    @Override
    public void updateUserProperties(User user, UpdateUserRequest updateUserRequest) {
        if (updateUserRequest.getFirstName() != null) {
            user.setFirstName(updateUserRequest.getFirstName());
        }
        if (updateUserRequest.getLastName() != null) {
            user.setLastName(updateUserRequest.getLastName());
        }
        if (updateUserRequest.getEmail() != null) {
            user.setEmail(updateUserRequest.getEmail());
        }
        if (updateUserRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }
        if (updateUserRequest.getRole() != null) {
            Role newRole = assignUserRole(updateUserRequest.getRole());
            user.setRole(newRole);
        }
    }

    @Override
    public List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }

    @Override
    public List<User> getActiveUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmIdAndIsActive(farmId, true);
    }

    @Override
    public Role assignUserRole(String strRole) {
        return switch (strRole) {
            case "ROLE_FARM_OWNER" -> roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                    .orElseThrow(() -> new RuntimeException("Rola: ROLE_FARM_OWNER nie została znaleziona"));
            case "ROLE_FARM_MANAGER" -> roleRepository.findByName(ERole.ROLE_FARM_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Rola: ROLE_FARM_MANAGER nie została znaleziona"));
            default -> roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR)
                    .orElseThrow(() -> new RuntimeException("Rola: ROLE_FARM_EQUIPMENT_OPERATOR nie została znaleziona"));
        };
    }
}