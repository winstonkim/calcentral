/*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
 */
package edu.berkeley.calcentral.system;

import com.google.common.collect.ImmutableMap;
import edu.berkeley.calcentral.services.CanvasProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

@Service
public class EnvJSONTag extends RequestContextAwareTag {
	private static final Log LOGGER = LogFactory.getLog(EnvJSONTag.class);

	@Override
	protected int doStartTagInternal() throws Exception {
		String envJSON;
		try {
			CanvasProxy canvasProxy = (CanvasProxy) getRequestContext().getWebApplicationContext().getBean("canvasProxy");
			ObjectMapper mapper = new ObjectMapper();
			envJSON = mapper.writeValueAsString(ImmutableMap.of(
					"canvasRoot", canvasProxy.getCanvasRoot()
			));
		} catch (Exception e) {
			LOGGER.error("Exception while mapping environmental data to JSON", e);
			envJSON = "{}";
		}
		pageContext.getOut().write(envJSON);
		return 0;
	}
}
