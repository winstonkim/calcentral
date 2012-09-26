package edu.berkeley.calcentral.controllers;

/**
 * Copyright (c) 2012 The Regents of the University of California
 */

import edu.berkeley.calcentral.Urls;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for backing simple JSPs with a simple ModelAndView that's used to inject user data in the form of JSON
 * into the page.
 */
@Controller
public class JSPController {

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = {Urls.DASHBOARD}, method = RequestMethod.GET)
	public ModelAndView getDashboard() {
		return new ModelAndView("dashboard");
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = {Urls.PROFILE}, method = RequestMethod.GET)
	public ModelAndView getProfile() {
		return new ModelAndView("profile");
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = {Urls.PREFERENCES}, method = RequestMethod.GET)
	public ModelAndView getPreferences() {
		return new ModelAndView("preferences");
	}
}
