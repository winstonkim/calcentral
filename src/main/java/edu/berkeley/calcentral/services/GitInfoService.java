package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.domain.GitInfo;
import org.jboss.resteasy.annotations.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Service
@Path(Urls.GIT_INFO)
public class GitInfoService {

	@Autowired
	private GitInfo gitInfo;

	@Cache(mustRevalidate = true)
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public GitInfo get() {
		return this.gitInfo;
	}

}
