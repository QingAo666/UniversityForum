package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVo;
import com.example.service.WeatherService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate rest;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${spring.weather.key}")
    String key;


    //通过经纬度获取当前位置天气信息
    @Override
    public WeatherVo fetchWeather(double longitude, double latitude) {
        return fetchFromCache(longitude,latitude);
    }

    //从缓存中取某个时间段相同的城市天气信息，这样可以减少对天气接口的访问次数
    //在缓存中存城市的id 而不是经纬度
    private WeatherVo fetchFromCache(double longitude, double latitude){
        //根据和风天气可知获取的是压缩后的数据
        //直接访问restful服务
        JSONObject geo = this.decompressStingToJson(rest.getForObject
                ("https://geoapi.qweather.com/v2/city/lookup?location="+longitude+","+latitude+"&key="+key,byte[].class));
        if(geo == null) return null;
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);
        //location里面有城市id
        int id = location.getInteger("id");
        String key = Const.FORUM_WEATHER_CACHE + id;
        String cache = redisTemplate.opsForValue().get(key);
        if(cache != null)
            return JSONObject.parseObject(cache).to(WeatherVo.class);
        WeatherVo vo = this.fetchFromAPI(id,location);
        if(vo == null) return null;
        redisTemplate.opsForValue().set(key,JSONObject.from(vo).toJSONString(),1, TimeUnit.HOURS);
        return vo;
    }

    //从Api中读取天气信息
    private WeatherVo fetchFromAPI(int id,JSONObject location){
        WeatherVo vo = new WeatherVo();
        vo.setLocation(location);
        JSONObject now = this.decompressStingToJson(rest.getForObject
                ("https://devapi.qweather.com/v7/weather/now?location="+id+"&key="+key,byte[].class));
        if(now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        JSONObject hourly = this.decompressStingToJson(rest.getForObject
                ("https://devapi.qweather.com/v7/weather/24h?location="+id+"&key="+key,byte[].class));
        if(hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;
    }

    //解压缩读取
    private JSONObject decompressStingToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while((read = gzip.read(buffer)) != -1){
                stream.write(buffer,0,read);
            }
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        }catch (IOException e){
            return null;
        }
    }
}
