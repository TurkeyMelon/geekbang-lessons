package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.service.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author Geoffrey
 */
@Path("/sign-in")
public class SignInActionController implements PageController {

    private final UserService userService = new UserServiceImpl();

    @POST
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        User loginUser = userService.queryUserByNameAndPassword(name, password);
        if (loginUser != null) {
            System.out.println("Login user: " + loginUser);
            return "login-success.jsp";
        }
        return "login-fail.jsp";
    }
}
