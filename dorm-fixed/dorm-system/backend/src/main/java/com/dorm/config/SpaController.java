package com.dorm.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Vue Router history 模式兜底：
 * 所有非 /api 的请求都返回 index.html，
 * 让前端路由接管。
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/login",
        "/dashboard",
        "/buildings",
        "/rooms",
        "/students",
        "/repairs",
        "/visitors",
        "/fees",
        "/notices",
        "/users"
    })
    public String index() {
        return "forward:/index.html";
    }
}
