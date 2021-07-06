package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
/*
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
*/



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoApplicationTests {

	@RequestMapping("/demo")
	public String demo() {
		return "Welcome to Yawin Tutor";
	}

}