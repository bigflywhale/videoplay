package com.flymonkey.fileserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: flymonkey
 * Date: 2021/3/30
 * Time: 17:11
 * Description:
 */
@Controller
//@RequestMapping("login")
public class LoginController {

    @RequestMapping("/index")
    public String login() {
        return "index";
    }
}
