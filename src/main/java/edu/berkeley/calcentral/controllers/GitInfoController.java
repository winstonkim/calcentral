package edu.berkeley.calcentral.controllers;

import edu.berkeley.calcentral.RESTConstants;
import edu.berkeley.calcentral.domain.GitInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Controller
@Path(RESTConstants.PATH_API + "/gitInfo")
public class GitInfoController {

	@Autowired
	private GitInfo gitInfo;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public GitInfo get() {
		return this.gitInfo;
	}

}
