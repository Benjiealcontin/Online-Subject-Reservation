package com.Reservation.SubjectService.Util;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperUtil {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}