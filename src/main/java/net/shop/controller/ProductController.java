package net.shop.controller;


import lombok.Getter;
import net.shop.model.Product;
import net.shop.model.User;
import net.shop.service.ProductService;
import net.shop.service.SecurityService;
import net.shop.service.UserService;
import net.shop.util.AuthenticateException;
import net.shop.util.Hello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping(value = "products")
@Getter
public class ProductController {

    private ProductService productService;
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

    @Autowired(required = true)
    @Qualifier(value = "productService")
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * @param req
     * @param resp
     * @return products page
     */
    @RequestMapping(method = RequestMethod.GET)
    public String listProducts(HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("user", loggedUser);
            req.setAttribute("loggedUser", loggedUser.getLogin());
            Hello.userLogin = loggedUser.getLogin();
        } catch (AuthenticateException e) {
            req.setAttribute("user", new User());
            req.setAttribute("loggedUser", "Unsigned user");
        }

        req.setAttribute("product", new Product());
        req.setAttribute("listProducts", this.productService.listProducts());

        return "products";
    }

    /**
     * @param req
     * @param resp
     * @return products page with added to order product
     */
    @RequestMapping(value = "/addtoorder/{productId}", method = RequestMethod.GET)
    public String addToOrder(HttpServletRequest req, HttpServletResponse resp) {
        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "You need to get authorized first");
            return "authorization";
        }

        int userId = loggedUser.getId();
        int productId = Integer.valueOf(req.getRequestURI().split("products/addtoorder/")[1]);
        boolean result = getProductService().addToOrder(loggedUser, productId);
//        if (result)
//            req.setAttribute("result", "product added");
//        else
//            req.setAttribute("result", "product was not added");

        return "redirect:/products";
    }

    /**
     * @param req
     * @param resp
     * @return product details page
     */
    @RequestMapping("/{id}")
    public String productData(HttpServletRequest req, HttpServletResponse resp) {
        int productId = Integer.valueOf(req.getRequestURI().split("products/")[1]);
        req.setAttribute("product", this.productService.getById(productId));

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
            Hello.userLogin = loggedUser.getLogin();
        } catch (AuthenticateException e) {
            req.setAttribute("user", new User());
        }

        return "productdata";
    }

    /**
     * @param req
     * @param resp
     * @return products page with added product
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(@ModelAttribute Product product, HttpServletRequest req, HttpServletResponse resp) {
        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "Please, get authorized first");
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage products list");
            return "redirect:/products";
        }

        Product createdProduct = this.productService.add(product);

        if (createdProduct == null) {
            req.setAttribute("exception", "Product already exists");
            req.setAttribute("product", new Product());
            req.setAttribute("listUsers", this.productService.listProducts());
            return "exception";
        }

        return "redirect:/products";
    }

    /**
     * @param req
     * @param resp
     * @return products page with edited product
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String update(@ModelAttribute Product product,
                         HttpServletRequest req, HttpServletResponse resp) {
        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "You need to get authorized first ");
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage products list");
            return "redirect:/products";
        }

        Product updatingProduct = new Product(req.getParameter("name"), req.getParameter("code"),
                Long.parseLong(req.getParameter("price")), "USD");
        String strProductId = req.getParameter("id");

        updatingProduct.setId(Integer.valueOf(strProductId));
        this.productService.update(updatingProduct);

        return "redirect:/products";
    }

    /**
     * @param req
     * @param resp
     * @return products page with product edit form
     */
    @RequestMapping(value = "edit/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable ("id") Integer productId,
                       HttpServletRequest req, HttpServletResponse resp) {

        User loggedUser = null;

        try {
            loggedUser = getSecurityService().authenticate(req, resp);
            req.setAttribute("loggedUser", loggedUser.getLogin());
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "Please, get authorized first ");
            return "authorization";
        }

        if (!loggedUser.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage products list");
            return "redirect:/products";
        }

//        int productId = Integer.valueOf(req.getRequestURI().split("products/edit/")[1]);
        req.setAttribute("user", loggedUser);
        req.setAttribute("product", this.productService.getById(productId));
        req.setAttribute("listProducts", this.productService.listProducts());

        return "products";
    }

    /**
     * @param req
     * @param resp
     * @return products page without deleted product
     */
    @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
    public String remove(HttpServletRequest req, HttpServletResponse resp) {

        User user = null;

        try {
            user = getSecurityService().authenticate(req, resp);
        } catch (AuthenticateException e) {
            req.setAttribute("exception", "Please, get authorized first");
            return "authorization";
        }

        if (!user.getAdmin()) {
            req.setAttribute("exception", "Only admin can manage products list");
            return "redirect:/products";
        }

        int productId = Integer.valueOf(req.getRequestURI().split("products/remove/")[1]);
        ;
        this.productService.remove(productId);

        return "redirect:/products";
    }

}
