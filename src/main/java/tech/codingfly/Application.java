package tech.codingfly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        redisTemplate.hasKey("aaaaaaaaaa");
        redisTemplate.opsForValue().set("Aaaaaaaaa", new HashMap() {{ put("aa","aa"); put("bb", "Bbb"); }});
        Map map = (Map) redisTemplate.opsForValue().get("Aaaaaaaaa");
        System.out.println(map);
    }

}
