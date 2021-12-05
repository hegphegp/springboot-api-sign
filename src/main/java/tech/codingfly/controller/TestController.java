package tech.codingfly.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class TestController {

    @GetMapping("/get")
    public Map get() {
        Map map = new HashMap();
        map.put("111", UUID.randomUUID().toString());
        map.put("code", 200);
        return map;
    }

    @PostMapping("/post")
    public Map post(@RequestBody Map params) {
        Map map = new HashMap();
        map.put("111", UUID.randomUUID().toString());
        map.put("code", 200);
        return map;
    }

}
