package com.project.ExamResultBackend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate redisTemplate;

    public void set(String key, Object object, Long ttl ){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(object);
            redisTemplate.opsForValue().set(key, json, ttl, TimeUnit.SECONDS);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public<T> T get(String key, Class<T> entityClass){
        try{
            Object object = redisTemplate.opsForValue().get(key);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(object.toString(), entityClass);
        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }

    public void delete(String key){
        try{
            redisTemplate.delete(key);
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
