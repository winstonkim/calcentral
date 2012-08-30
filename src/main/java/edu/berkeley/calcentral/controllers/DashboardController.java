package edu.berkeley.calcentral.controllers;

/**
 * DashboardController.java
 * Copyright (c) 2012 The Regents of the University of California
 */

import javax.servlet.http.HttpServletRequest;

import edu.berkeley.calcentral.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.berkeley.calcentral.daos.UserDataDao;

/**
 * Controller for pulling up the initial dashboard after a user logs in.
 */
@Controller
public class DashboardController {

	@Autowired
	private UserDataDao userDataDao;

	/**
	 * GET call for the dashboard.
	 *
	 * @param model map to return to the view.
	 * @param request servlet request object.
	 * @return dashboard view.
	 */
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = { Urls.DASHBOARD }, method = RequestMethod.GET)
	public ModelAndView getDashboard(
			HttpServletRequest request) {
		return new ModelAndView("dashboard");
	}
}
