package cn.edu.fzu.sosd.web.sosdblog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token拦截器配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new SaInterceptor(handle -> {

            SaRouter.match("/**")
                    .notMatch("/user/login")
                    .notMatch("/user/register")
                    .notMatch("/swagger-ui/**")
                    .notMatch("/v3/api-docs/**")
                    .notMatch("/doc.html")
                    .notMatch("/webjars/**")
                    .notMatch("/favicon.ico")
                    .notMatch("/error")
                    .notMatch("/static/**")
                    .check(r -> StpUtil.checkLogin());

//            SaRouter.match("/admin/**", r -> StpUtil.checkRole("ROLE_ADMIN"));
//            SaRouter.match("/article/edit/**", r -> StpUtil.checkPermission("article:edit"));
//            SaRouter.match("/article/delete/**", r -> StpUtil.checkPermission("article:delete"));
        })).addPathPatterns("/**");
    }
}