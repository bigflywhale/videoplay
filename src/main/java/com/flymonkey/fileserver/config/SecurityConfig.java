package com.flymonkey.fileserver.config;

import com.flymonkey.fileserver.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf防护
                .csrf().disable()
                .headers().frameOptions().disable()
                .and();
        http
                //登录处理
                .formLogin() //表单方式，或httpBasic
                .loginPage("/index")
                .loginProcessingUrl("/form")
                .defaultSuccessUrl("/audio/welcome") //成功登陆后跳转页面
                .failureUrl("/loginError")
                .permitAll()
                .and();
        http
                .authorizeRequests() // 授权配置
                //无需权限访问
                .antMatchers("/css/**", "/error404").permitAll()
                .antMatchers("/user/**").hasRole("USER")
                //其他接口需要登录后才能访问
                .anyRequest().authenticated()
                .and();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                //用户认证处理
                .userDetailsService(userDetailsService)
                //密码处理
                .passwordEncoder(passwordEncoder());
    }
}