package com.example.service;

import com.example.entity.vo.response.WeatherVo;
import org.springframework.stereotype.Service;

@Service
public interface WeatherService {

    WeatherVo fetchWeather(double longitude,double latitude);
}
