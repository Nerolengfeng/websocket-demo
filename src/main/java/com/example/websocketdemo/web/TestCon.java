package com.example.websocketdemo.web;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TestCon {

    @RequestMapping("test")
    public List<Long> test(@RequestParam("a[]") List<Long> a){
        for (Long b: a
             ) {
            System.out.println(b);
        }
        return a;
    }

    public static void main(String[] args) {
        String a = null;

        System.out.println(String.valueOf(a));
    }
}
