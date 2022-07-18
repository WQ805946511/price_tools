package com.smarting.price.tools.mapper;

import com.smarting.price.tools.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> getAuthenticate(@Param("userName") String userName, @Param("password") String password);

    List<User> getCurrentUer(@Param("email") String email);
}
