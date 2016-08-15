package net.shop.controller;

import lombok.Getter;
import net.shop.model.User;
import net.shop.model.mock.LoggedUserMock;
import net.shop.service.SecurityService;
import net.shop.service.UserService;
import net.shop.util.AuthenticateException;
import net.shop.util.AuthorizationException;
import net.shop.util.Hello;
import net.shop.util.PermissionException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;

@Controller
@RequestMapping(value = "users")
@Getter
public class UserController {

    private UserService userService;
    private SecurityService securityService;

    @Autowired(required = true)
    @Qualifier(value = "securityService")
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired(required = true)
    @Qualifier(value = "userService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page with users list
     */
    @RequestMapping(method = RequestMethod.GET)
    public String listUsers(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can get the list of users");
            return "redirect:/products";
        }

        loggedUser = new User(req.getParameter("login"), req.getParameter("password"),
                Boolean.parseBoolean(req.getParameter("admin")), Boolean.parseBoolean(req.getParameter("blocked")), new HashSet<>());
        req.setAttribute("user", new User());
        req.setAttribute("listUsers", this.userService.listUsers());

        return "users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page with added user
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can add new user");
            return "redirect:/products";
        }

        User creatingUser = new User(req.getParameter("login"), req.getParameter("password"),
                Boolean.parseBoolean(req.getParameter("admin")),
                Boolean.parseBoolean(req.getParameter("blocked")), new HashSet<>());

        try {
            this.userService.add(creatingUser);
        } catch (ConstraintViolationException e) {
            req.setAttribute("exception", "User already exists");
            req.setAttribute("user", new User());
            req.setAttribute("listUsers", this.userService.listUsers());
            return "users";
        }

        return "redirect:/users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page with edited user
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String update(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage users");
            return "redirect:/products";
        }

        User updatingUser = new User(req.getParameter("login"), req.getParameter("password"),
                Boolean.parseBoolean(req.getParameter("admin")), Boolean.parseBoolean(req.getParameter("blocked")), new HashSet<>());
        String strUserId = req.getParameter("id");

        updatingUser.setId(Integer.valueOf(strUserId));
        this.userService.update(updatingUser);

        return "redirect:/users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page with user edit form
     */
    @RequestMapping(value = "edit/{id}", method = RequestMethod.GET)
    public String getUserForEdit(HttpServletRequest req, HttpServletResponse resp) {
        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "You need to get authorized first");
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage users");
            return "redirect:/products";
        }

        User updatingUser = new User(req.getParameter("login"), req.getParameter("password"),
                Boolean.parseBoolean(req.getParameter("admin")), Boolean.parseBoolean(req.getParameter("blocked")), new HashSet<>());
        int userId = Integer.valueOf(req.getRequestURI().split("users/edit/")[1]);
        req.setAttribute("user", this.userService.getById(userId));
        req.setAttribute("listUsers", this.userService.listUsers());

//        updatingUser.setId(Integer.valueOf(userId));
//        this.userService.update(user);

        return "users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return blacklist page
     */
    @RequestMapping(value = "/blacklist", method = RequestMethod.GET)
    public String blackList(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "You need to get authorized first");
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can see the blacklist");
            return "redirect:/products";
        }

        loggedUser = new User(req.getParameter("login"), req.getParameter("password"),
                Boolean.parseBoolean(req.getParameter("admin")), Boolean.parseBoolean(req.getParameter("blocked")), new HashSet<>());
        req.setAttribute("user", new User());
        req.setAttribute("listUsers", this.userService.listUnpaidUsers());

        return "users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page with added to blacklist or removed from blacklist user
     */
    @RequestMapping(value = "/addtoblacklist/{id}", method = RequestMethod.GET)
    public String addUserToBlackList(HttpServletRequest req, HttpServletResponse resp) throws PermissionException {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
        } catch (AuthenticateException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can add users to the blacklist");
            return "redirect:/products";
        }

        int userId = Integer.valueOf(req.getRequestURI().split("addtoblacklist/")[1]);
        User editedUser = userService.getById(userId);
        getUserService().addUserToBlackList(editedUser, userId);

        return "redirect:/users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users page without deleted user
     */
    @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
    public String remove(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
        } catch (AuthenticateException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage users");
            return "redirect:/products";
        }

        int userId = Integer.valueOf(req.getRequestURI().split("users/remove/")[1]);
        this.userService.remove(userId);

        return "redirect:/users";
    }

    /**
     *
     * @param req
     * @param resp
     * @return users details page
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String productData(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = securityService.authorization(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
            Hello.userLogin = loggedUser.getLogin();
        } catch (AuthorizationException e) {
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can see users data");
            return "redirect:/products";
        }

        int userId = Integer.valueOf(req.getRequestURI().split("users/")[1]);
        req.setAttribute("user", this.userService.getById(userId));

        return "userdata";
    }

    /**
     *
     * @param req
     * @param resp
     * @return authorization page
     */
    @RequestMapping(value = "/authorization", method = RequestMethod.GET)
    public String authorizationView(HttpServletRequest req, HttpServletResponse resp) {
        return "authorization";
    }

    /**
     *
     * @param req
     * @param resp
     * @return registration page
     */
    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationView(HttpServletRequest req, HttpServletResponse resp) {
        return "registration";
    }

    /**
     *
     * @param req
     * @param resp
     * @return products page after successful authorization or authorization page if failed
     */
    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public String authorization(HttpServletRequest req, HttpServletResponse resp) {

        try {
            securityService.authorization(req, resp);
        } catch (AuthorizationException e) {
            req.setAttribute("exception", "Wrong login or password");
            return "authorization";
        }

        return "redirect:/products";
    }

    /**
     *
     * @param req
     * @param resp
     * @return products page after successful registration or registration page if failed
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String registration(HttpServletRequest req, HttpServletResponse resp) {

        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String passwordCheck = req.getParameter("passwordCheck");

        if (!password.equals(passwordCheck)) {
            req.setAttribute("exception", "Passwords don't match");
            return "registration";
        }

        User user = new User(login, password, false, false, new HashSet<>());

        try {
            userService.add(user);
        } catch (Exception e) {
            req.setAttribute("exception", "Login Already Used");
            return "registration";
        }
        try {
            securityService.authorization(req, resp);
        } catch (AuthorizationException e) {
            req.setAttribute("exception", "Authorization failed");
        }
        return "redirect:/products";
    }

    /**
     *
     * @param req
     * @param resp
     * @return authorization page
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest req, HttpServletResponse resp) {
        User loggedUser = null;

        try {
            loggedUser = securityService.authenticate(req, resp);
        } catch (AuthenticateException e) {
            return "authorization";
        }

        try {
            securityService.logout(req, resp, loggedUser);
            Hello.userLogin = "you can <a href=\"../../users/authorization\">SignIn</a> and get an account!";
        } catch (AuthorizationException e) {
            return "authorization";
        }
        return "authorization";
    }

}
