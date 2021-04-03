package com.flymonkey.fileserver.service;

import com.flymonkey.fileserver.mo.SysUser;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: flymonkey
 * Date: 2021/3/31
 * Time: 14:30
 * Description:
 */
@Service
public class SysUserService {
    public SysUser findUserByName(String username) {
        SysUser user = new SysUser();
        user.setName("123");
        user.setPassword(new BCryptPasswordEncoder().encode("liuzhilei"));
        return user;
    }
}
