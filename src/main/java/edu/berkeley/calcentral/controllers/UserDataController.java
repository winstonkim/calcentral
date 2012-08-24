/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.berkeley.calcentral.domain.CalCentralUser;
import edu.berkeley.calcentral.services.UserDataService;

@Controller
public class UserDataController {

    @Autowired
    private UserDataService userDataService;

    @RequestMapping(value = { "/currentUser" }, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getCurrentUser(
            Map<String, Object> model,
            HttpServletRequest request) {
        JSONObject response = new JSONObject();
        try {
            String uid = null;
            if (request.getUserPrincipal() == null) {
                response.put("loggedIn", false);
                return response.toString();
            }
            
            uid = request.getUserPrincipal().getName();
            response.put("loggedIn", true);
            CalCentralUser user = userDataService.get(uid);
            if (user == null) {
                return response.toString();
            }
            response.put("userId", user.getUid());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());            
        } catch (JSONException e) {
            //do nothing!
        }
        return response.toString();
    }
}
