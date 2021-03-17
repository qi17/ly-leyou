package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.mapper.UserMapper;
import com.leyou.pojo.User;
import com.leyou.utils.CodecUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper mapper;
    @Autowired
    private AmqpTemplate template;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:code:";

    /**
     * mapper返回为0 说明用户不存在
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {

        User record = new User();
//        判断type类型
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new lyException(ExceptionEnum.INVALID_USER_TYPE);
        }
        int count = mapper.selectCount(record);
        return count == 0;
    }

    /**
     * 发送验证码，通过rabbitmq进行消息的监控
     * @param phone
     */
    @Transactional
    public void checkCode(String phone) {
//        生成验证码
        String code = NumberUtils.generateCode(6);
        String key = KEY_PREFIX + phone;
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
//        发送广播消息
        template.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
//        保存验证码
        redisTemplate.opsForValue().set(key,code, 5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
//        1.校验验证码
        if(!code.equals(redisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone()))){
            throw new lyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
//      2.对密码进行加密处理----mod
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(DigestUtils.md5Hex(user.getPassword() + salt));

//        3.保存用户
        user.setCreated(new Date());
        mapper.insert(user);
    }

    public User queryUserByUsernameAndPassword(String username, String password) {
//        1.查询用户名是否存在
        User record = new User();
        record.setUsername(username);
        User user = mapper.selectOne(record);
        if(user == null){
            throw  new lyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
//        2.用户存在校验密码
        String salt = user.getSalt();//获取盐值
        if(!StringUtils.equals(user.getPassword(),DigestUtils.md5Hex(password + salt))){
            throw  new lyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }
}
