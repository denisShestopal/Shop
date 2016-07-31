package net.shop.controller;

import lombok.Getter;
import lombok.Setter;
import net.shop.model.User;
import net.shop.model.mock.LoggedUserMock;
import net.shop.service.UserService;
import net.shop.util.AuthException;
import net.shop.util.LoggedUserUtil;
import net.shop.util.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.SecondaryTable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
//@RequestMapping(value = "users")
@Getter
@Setter
public class UserController {

    private UserService userService;

    @Autowired(required = true)
    @Qualifier(value = "userService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users/add", method = RequestMethod.POST)
    public String add(@ModelAttribute("user") User user) {
        this.userService.add(user);
        return "redirect:/users";
    }

    @RequestMapping(value = "users", method = RequestMethod.GET)
    public String listUsers(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("listUsers", this.userService.listUsers());

        //return reference to the page "users"
        return "users";
    }

    @RequestMapping(value = "/users/blacklist", method = RequestMethod.GET)
    public String blackList(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("listUsers", this.userService.listUnpaidUsers());

        //return reference to the page "products"
        return "users";
    }

    @RequestMapping(value = "/users/addtoblacklist/{id}", method = RequestMethod.GET)
    public String addUserToBlackList(HttpServletRequest request, HttpServletResponse response) throws AuthException, PermissionException {
//        int loggedUserId = userService.getUserIdFromRequest(request);
//        User loggedUser = userService.getById(loggedUserId);
        int userId = Integer.valueOf(request.getRequestURI().split("addtoblacklist/")[1]);
//        String[] strUri = request.getRequestURI().split("userId=");
//        int userId = Integer.valueOf(strUri[1]);
        User admin = new LoggedUserMock();

        getUserService().addUserToBlackList(admin, userId);

        return "redirect:/users";

    }

    @RequestMapping("/removeuser/{id}")
    public String remove(@PathVariable("id") int id) {
        //TODO get user by id and user's authority. if admin ? next : exception
        this.userService.remove(id);

        return "redirect:/users";
    }

    @RequestMapping("edituser/{id}")
    //TODO get user by id and user's authority. if admin ? next : exception
    public String edit(@PathVariable("id") int id, Model model) {
        model.addAttribute("users", this.userService.getById(id));
        model.addAttribute("listUsers", this.userService.listUsers());

        return "users";
    }
}
