package com.saint.dynamic.mapper;


import com.saint.dynamic.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(UserDTO userDTO);

    void update(UserDTO userDTO);

    int creates(List<UserDTO> userDTOS);

}
