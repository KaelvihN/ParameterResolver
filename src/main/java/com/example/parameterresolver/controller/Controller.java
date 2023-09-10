package com.example.parameterresolver.controller;

import com.example.parameterresolver.pojo.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author : KaelvihN
 * @date : 2023/9/10 11:31
 */
public class Controller {
    public static void testController(
            @RequestParam("name1") String name1, //name1 = KaelvihN
            String name2, //name2 = AraneidaSword
            @RequestParam("age") int age, //age = 18
            @RequestParam(name = "home", defaultValue = "${JAVA_HOME}") String home1, //Spring获取数据
            @RequestParam("file") MultipartFile file, //上传文件
            @PathVariable("id") int id, //url 123
            @RequestHeader("Content-Type") String header, //request header type = application/json
            @CookieValue("token") String token, //token 123456
            @Value("${JAVA_HOME}") String home2, //Spring获取 ${} 和 #{}
            HttpServletRequest request,  //request，response，session...
            @ModelAttribute(value = "abc") User user, //user => name = 张三 ，age = 18
            User user2, //user => name = 张三 ，age =18
            @RequestBody User user3 //user = 李四 ，age =20(Json)
    ) {
    }
}
