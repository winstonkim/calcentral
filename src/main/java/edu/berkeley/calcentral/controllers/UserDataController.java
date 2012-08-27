/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.berkeley.calcentral.RESTConstants;
import edu.berkeley.calcentral.services.UserDataService;

@Controller
@Path(RESTConstants.PATH_API)
public class UserDataController {

    private ObjectMapper jMapper = new ObjectMapper();

    @Autowired
    private UserDataService userDataService;

    @GET
    @Path("currentUser")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCurrentUser(
            @Context HttpServletRequest request) {
        ObjectNode responseNode = jMapper.getNodeFactory().objectNode();
        if (request.getUserPrincipal() == null) {
            responseNode.put("loggedIn", false);
            return responseNode.toString();
        } else {
            responseNode.put("loggedIn", true);
        }

        String uid = request.getUserPrincipal().getName();
        ObjectNode userNode = userDataService.getUserAndWidgetData(uid);
        responseNode.putAll(userNode);
        return responseNode.toString();
    }
    
    @GET
    @Path("user/{userID}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getUser(@PathParam(RESTConstants.PARAM_USER_ID) String userID) {
        ObjectNode responseNode = userDataService.getUserAndWidgetData(userID);
        if (responseNode == null) {
            return null;
        } else {
            return responseNode.toString();
        }
    }
    
    @POST
    @Path("user/{userID}")
    @Produces({MediaType.APPLICATION_JSON})
    public String saveUserAndWidgetData(@PathParam(RESTConstants.PARAM_USER_ID) String userID,
            @FormParam(RESTConstants.PARAM_DATA) String jsonData) {
        String savedData = null;
        savedData = userDataService.saveUser(jsonData);
        return savedData;
    }
    
    @DELETE
    @Path("user/{userID}")
    public void deleteUserAndWidgetData(@PathParam(RESTConstants.PARAM_USER_ID) String userID) {
        userDataService.deleteUserAndWidgetData(userID);
    }
    
}
