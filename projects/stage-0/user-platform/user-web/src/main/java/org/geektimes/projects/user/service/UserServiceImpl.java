package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.DatabaseUserRepository;
import org.geektimes.projects.user.repository.UserRepository;

public class UserServiceImpl implements UserService{

    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserServiceImpl() {
        this.userRepository = new DatabaseUserRepository();
    }

    @Override
    public boolean register(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return this.userRepository.getByNameAndPassword(name, password);
    }
}
