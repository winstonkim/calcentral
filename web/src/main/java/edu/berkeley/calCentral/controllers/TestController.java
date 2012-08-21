package edu.berkeley.calCentral.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestController {

	@RequestMapping(value = { "/test" }, method = RequestMethod.GET)
	public String showTest(
			Map<String, Object> model,
			HttpServletRequest request) {
		model.put("foo", "bar");
		return "test/test";
	}
}
