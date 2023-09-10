package com.example.parameterresolver;

import com.example.parameterresolver.config.Config;
import com.example.parameterresolver.controller.Controller;
import com.example.parameterresolver.pojo.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExpressionValueMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ParameterResolverApplication {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class);
        //获取默认的BeanFactory，用于解析本地常量
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        //准备测试Request
        HttpServletRequest httpServletRequest = mockRequest();
        //1.控制器方法封装为HandlerMethod
        HandlerMethod handlerMethod = new HandlerMethod(new Controller(),
                Controller.class.getMethod("testController", String.class, String.class, int.class,
                        String.class, MultipartFile.class, int.class, String.class, String.class,
                        String.class, HttpServletRequest.class, User.class, User.class, User.class));
        //2.准备对象与类型转换
        ServletRequestDataBinderFactory binderFactory =
                new ServletRequestDataBinderFactory(null, null);
        //3.准备ModelAndViewContainer 来存储 Model 结果
        ModelAndViewContainer container = new ModelAndViewContainer();
        //4.解析每个参数
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            //获取解析器集合
            HandlerMethodArgumentResolverComposite composite = addResolverList(beanFactory);
            //获取每个参数的下标索引
            int parameterIndex = parameter.getParameterIndex();
            //获取每个参数的注解
            String annotationName = Arrays.stream(parameter.getParameterAnnotations())
                    .map(annotation -> annotation.annotationType().getSimpleName())
                    .collect(Collectors.joining());
            annotationName = (annotationName.length() == 0) ? "null" : "@" + annotationName;
            //获取每个参数的类型
            String parameterType = parameter.getParameterType().getSimpleName();
            //添加一个解析参数名的解析器
            parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
            //获取每个参数的名字
            String parameterName = parameter.getParameterName();
            //输出
            if (composite.supportsParameter(parameter)) {
                //方法信息
                System.out.print("[" + parameterIndex + "]\t" + annotationName + "\t" + parameterType + "\t" + parameterName);
                System.out.print("=>");
                //解析传入的参数
                //参数对象
                Object argument = composite.resolveArgument(parameter, container,
                        new ServletWebRequest(httpServletRequest), binderFactory);
                //参数类型
                String typeName = argument.getClass().getSimpleName();
                //输出传入参数
                System.out.println(typeName + "\t" + argument);
                System.out.println("模型数据=>" + container.getModel());
            } else {
                //方法信息
                System.out.println("[" + parameterIndex + "]\t" + annotationName + "\t" + parameterType + "\t" + parameterName);
            }
        }
    }

    private static HandlerMethodArgumentResolverComposite addResolverList(DefaultListableBeanFactory beanFactory) {
        //解析@RequestParam
        RequestParamMethodArgumentResolver requestParamMethodArgumentResolver =
                new RequestParamMethodArgumentResolver(beanFactory, false);
        //解析@PathVariable
        PathVariableMethodArgumentResolver pathVariableMethodArgumentResolver =
                new PathVariableMethodArgumentResolver();
        //解析@RequestHeader
        RequestHeaderMethodArgumentResolver requestHeaderMethodArgumentResolver =
                new RequestHeaderMethodArgumentResolver(beanFactory);
        //解析@CookieValue
        ServletCookieValueMethodArgumentResolver cookieValueMethodArgumentResolver =
                new ServletCookieValueMethodArgumentResolver(beanFactory);
        //解析@Value
        ExpressionValueMethodArgumentResolver expressionValueMethodArgumentResolver =
                new ExpressionValueMethodArgumentResolver(beanFactory);
        //解析HttpServletRequest
        ServletRequestMethodArgumentResolver servletRequestMethodArgumentResolver =
                new ServletRequestMethodArgumentResolver();
        //解析@ModleAttribute
        ServletModelAttributeMethodProcessor servletModelAttributeMethodProcessor =
                new ServletModelAttributeMethodProcessor(false);
        //解析@RequestBody
        RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor =
                new RequestResponseBodyMethodProcessor(List.of(new MappingJackson2HttpMessageConverter()));
        //解析默认的对象但是没加@ModleAttribute
        ServletModelAttributeMethodProcessor defaultServletModelAttributeMethodProcessor =
                new ServletModelAttributeMethodProcessor(true);
        //解析默认的变量没加@RequestParam
        RequestParamMethodArgumentResolver defaultRequestParamMethodArgumentResolver =
                new RequestParamMethodArgumentResolver(true);
        //将解析器加入到HandlerMethodArgumentResolverComposite(本质上是加入到一个ArrayList中),方便调用
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();

        composite.addResolvers(requestParamMethodArgumentResolver,
                pathVariableMethodArgumentResolver,
                requestHeaderMethodArgumentResolver,
                cookieValueMethodArgumentResolver,
                expressionValueMethodArgumentResolver,
                servletRequestMethodArgumentResolver,
                servletModelAttributeMethodProcessor,
                requestResponseBodyMethodProcessor,
                defaultServletModelAttributeMethodProcessor,
                defaultRequestParamMethodArgumentResolver);
        return composite;
    }

    private static HttpServletRequest mockRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name1", "KaelvihN");
        request.setParameter("name2", "AraneidaSword");
        request.addPart(new MockPart("file", "abc", "hello".getBytes(StandardCharsets.UTF_8)));
        Map<String, String> map = new AntPathMatcher().extractUriTemplateVariables("/test/{id}", "/test/123");
        System.out.println(map);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, map);
        request.setContentType("application/json");
        request.setCookies(new Cookie("token", "123456"));
        request.setParameter("name", "张三");
        request.setParameter("age", "18");
        request.setContent("""
                    {
                        "name":"李四",
                        "age":20
                    }
                """.getBytes(StandardCharsets.UTF_8));
        return new StandardServletMultipartResolver().resolveMultipart(request);
    }
}
