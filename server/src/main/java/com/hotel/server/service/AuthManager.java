package com.hotel.server.service;

import com.hotel.common.LoginResponse;
import com.hotel.common.RegisterUserRequest;
import com.hotel.common.RegisterUserResponse;
import com.hotel.server.dao.UserDao;
import com.hotel.server.model.User;

public class AuthManager {
    private static AuthManager instance;
    private final UserDao userDao = new UserDao();

    private AuthManager() { }

    public static synchronized AuthManager getInstance() {
        if (instance == null) instance = new AuthManager();
        return instance;
    }

    public LoginResponse authenticate(String username, String password) {
        return userDao.findByUsername(username)
                .map(user -> {
                    if (user.getPasswordHash().equals(password)) {
                        return new LoginResponse(true, user.getUserId(), user.getRoleName(), "OK");
                    } else {
                        return new LoginResponse(false, 0, null, "Неверный пароль");
                    }
                })
                .orElse(new LoginResponse(false, 0, null, "Пользователь не найден"));
    }

    public RegisterUserResponse registerUser(RegisterUserRequest req) {
        if (userDao.existsByUsername(req.getUsername())) {
            return new RegisterUserResponse(false, "Имя занято");
        }
        if (userDao.existsByEmail(req.getEmail())) {
            return new RegisterUserResponse(false, "E-mail уже зарегистрирован");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(req.getPassword());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setFullName(req.getFullName());
        user.setCardNumber(req.getCardNumber());
        user.setCardExpiry(req.getCardExpiry());
        user.setCvv(req.getCvv());
        user.setRoleId(userDao.findRoleIdByName("GUEST"));

        boolean ok = userDao.create(user);
        return new RegisterUserResponse(ok, ok ? "Регистрация успешна" : "Ошибка базы данных");
    }
}
