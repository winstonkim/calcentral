/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserDataController {

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = { "/currentuser" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getCurrentUser(
			Map<String, Object> model,
			HttpServletRequest request) {
		String uid = request.getUserPrincipal().getName();
		return uid;
	}
}
