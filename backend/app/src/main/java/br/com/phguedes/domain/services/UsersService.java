package br.com.phguedes.domain.services;

import br.com.phguedes.domain.entities.Users;
import br.com.phguedes.domain.repositories.UsersRepository;
import br.com.phguedes.domain.validation.UsersValidation;
import br.com.phguedes.dto.AuthDto;
import br.com.phguedes.dto.UsersDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UsersService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final UsersValidation usersValidation;
    private final PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRED)
    public UsersDto createUser(UsersDto dto) {
        usersValidation.emailAlreadyExists(dto.getEmail());

        Users user = Users.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(this.passwordEncoder.encode(dto.getPassword()))
                .build();

        log.info("Saving user: {}", user);
        return toDto(usersRepository.save(user));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UsersDto updateUser(Long id, UsersDto dto) {
        Users existingUser = this.usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!existingUser.getEmail().equals(dto.getEmail())) {
            usersValidation.emailAlreadyExists(dto.getEmail());
        }

        Users updatedUser = existingUser.toBuilder()
                .name(dto.getName() != null ? dto.getName() : existingUser.getName())
                .email(dto.getEmail() != null ? dto.getEmail() : existingUser.getEmail())
                .password(dto.getPassword() != null && !dto.getPassword().isBlank()
                        ? this.passwordEncoder.encode(dto.getPassword())
                        : existingUser.getPassword())
                .build();

        log.info("Updating user: {}", updatedUser.getId());
        return toDto(usersRepository.save(updatedUser));
    }

    @Transactional(readOnly = true)
    public UsersDto getUserById(long id) {
        Users user = this.usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        log.info("Showing user: {}", id);
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return usersRepository.count();
    }

    @Transactional(readOnly = true)
    public List<UsersDto> getAllUser() {
        log.info("Showing all users");
        return this.usersRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private UsersDto toDto(Users user) {
        return UsersDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> usersOptional = this.usersRepository.findByEmail(username);

        return usersOptional.map(users ->
                new User(users.getEmail(), users.getPassword(), new ArrayList<GrantedAuthority>())
        ).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuthDto auth(AuthDto authDto) {
        Users user = usersValidation.findByEmail(authDto.getEmail());

        if (!this.passwordEncoder.matches(authDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        StringBuilder password = new StringBuilder().append(user.getEmail()).append(":").append(user.getPassword());

        return AuthDto.builder()
                .email(user.getEmail())
                .token(Base64.getEncoder().withoutPadding().encodeToString(password.toString().getBytes()))
                .id(user.getId())
                .build();
    }
}
