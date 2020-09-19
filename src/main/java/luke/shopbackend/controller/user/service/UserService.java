package luke.shopbackend.controller.user.service;

import luke.shopbackend.model.entity.Role;
import luke.shopbackend.model.entity.User;
import luke.shopbackend.model.data_transfer.UserRequest;
import luke.shopbackend.repository.RoleRepository;
import luke.shopbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return All users in database. Data without password.
     */
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        userRepository.findAll().forEach(allUsers::add);
        return allUsers;
    }

    /**
     *
     * Will:
     *   - validate data from user, using helper methods
     *   - add ROLE_USER to new user
     *   - encrypt the user send password
     *   - persist user in database
     */
    public User addUser(UserRequest userRequest) {
        validateRegisterData(userRequest);

        Role role = getUserRole();
        User user = new User(userRequest);
        user.getRoles().add(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     *
     * Will throw exception if:
     * a) user has set ID
     * b) username already exists in database
     * c) email already exists in database
     */
    private void validateRegisterData(UserRequest request) {
        if (request.getId() != null)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Użytkownik z ustawionym ID nie może być zapisany w bazie.");

        Optional<User> optionalUser1 = userRepository.findByUsername(request.getUsername());
        if (optionalUser1.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Użytkownik z takim imieniem już istnieje w bazie.");

        Optional<User> optionalUser2 = userRepository.findByEmail(request.getEmail());
        if (optionalUser2.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Taki email już istnieje w bazie");
    }

    /**
     *
     * @return  ROLE_USER for every new created User.
     */
    private Role getUserRole(){
        Optional<Role> optional = roleRepository.findById(2L);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find user roles");
        }
        return optional.get();
    }
}
