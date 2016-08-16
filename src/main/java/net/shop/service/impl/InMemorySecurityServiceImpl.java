package net.shop.service.impl;

import net.shop.dao.UserDao;
import net.shop.model.User;
import net.shop.service.SecurityService;
import net.shop.util.AuthenticateException;
import net.shop.util.AuthorizationException;
import net.shop.util.Hello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySecurityServiceImpl implements SecurityService {

    private UserDao userDao;

    private Map<String, Integer> usersTokenMap = new ConcurrentHashMap<>();

    @Autowired(required = true)
    @Qualifier(value = "userDao")
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public User authenticate(HttpServletRequest req, HttpServletResponse resp) throws AuthenticateException {

        if (req.getCookies() == null) {
            req.setAttribute("exception", "Not authorized yet");
            throw new AuthenticateException();
        }

        Cookie[] cookies = req.getCookies();

        User user = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN))
                if (usersTokenMap.containsKey(cookie.getValue()))
                    user = userDao.getById(usersTokenMap.get(cookie.getValue()));
        }
        if (user == null) {
            req.setAttribute("exception", "Please get authorized!");
            throw new AuthenticateException();
        }


        Hello.userLogin = user.getLogin();
        //erase credentials after authentication fo security
        return new User(user);
    }

    @Override
    @Transactional
    public User authorization(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        User user = userDao.getUserByLogin(login);
        password = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());

        if (!user.getPassword().equals(password)) {
            req.setAttribute("exception", "Login or/and password are incorrect");
            throw new AuthorizationException("Login or/and password are incorrect");
        }

        String token = UUID.randomUUID().toString();
        usersTokenMap.put(token, user.getId());
        Cookie cookie = new Cookie(TOKEN, token);
        cookie.setPath("/");
        resp.addCookie(cookie);
        return new User(user);
    }

    @Override
    public boolean logout(HttpServletRequest req, HttpServletResponse resp, User user) throws AuthorizationException {
        usersTokenMap.entrySet().remove(user.getId());
        Cookie cookie = new Cookie(TOKEN, "deleted");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        return true;
    }
}
