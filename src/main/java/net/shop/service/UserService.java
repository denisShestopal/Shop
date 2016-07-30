package net.shop.service;


import net.shop.model.User;
import net.shop.util.AuthException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends BaseService<User> {
    List<User> listUnpaidUsers();
    int getUserIdFromRequest(HttpServletRequest request) throws AuthException;
}
