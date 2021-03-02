package org.geektimes.projects.user.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.derby.iapi.util.StringUtil;
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
@Path("/register")
public class RegisterActionController implements PageController {

    private final UserService userService = new UserServiceImpl();

    @POST
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        if (StringUtils.isBlank(name) || StringUtils.isBlank(password) || StringUtils.isBlank(email) || StringUtils.isBlank(phoneNumber)) {
            return "fail.jsp";
        }
        if (userService.register(new User(name, password, email, phoneNumber))) {
            return "register-success.jsp";
        }
        return "fail.jsp";
    }
}
