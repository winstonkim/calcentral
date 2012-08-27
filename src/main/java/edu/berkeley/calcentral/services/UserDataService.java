package edu.berkeley.calcentral.services;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.domain.CalCentralUser;
import edu.berkeley.calcentral.domain.WidgetData;

@Service
public class UserDataService {

    private ObjectMapper jMapper = new ObjectMapper();

    @Autowired
    private UserDataDao userDataDao;

    @Autowired
    private WidgetDataService widgetDataService;

    public CalCentralUser get(String uid) {
        return userDataDao.get(uid);
    }

    public void delete(String uid) {
        userDataDao.delete(uid);
    }

    public ObjectNode getUserAndWidgetData(String uid) {
        return generateUserJson(uid);
    }

    /**
     * Trying not to pollute the domain package with too many hybrid beans at the moment.
     * Can refactor this to do the collapsing inside a dao later if necessary.
     */
    private ObjectNode generateUserJson(String uid) {
        ObjectNode responseNode = jMapper.getNodeFactory().objectNode();
        CalCentralUser user = get(uid);
        if (user == null) {
            return null;
        }
        responseNode.putAll((ObjectNode) jMapper.valueToTree(user));
        responseNode.remove("activeFlag");

        List<WidgetData> widgetData =  widgetDataService.getAllForUser(uid);
        ArrayNode widgetDataNode = responseNode.putArray("widgetData");
        if (widgetData != null) {
            for (WidgetData someWidgetData : widgetData) {
                widgetDataNode.add((ObjectNode) jMapper.valueToTree(someWidgetData));
            }
        }

        return responseNode;
    }

    public String saveUser(String jsonData) {
        CalCentralUser userToSave = null;
        String savedUser = null;
        
        try {
            //making sure items serialize and deserialize properly before attempting to save.
            userToSave = jMapper.readValue(jsonData, CalCentralUser.class);
            savedUser = jMapper.writeValueAsString(userToSave);
            userDataDao.update(userToSave);
            return savedUser;
        } catch(Exception e) {
            //ignore malformed data.
            return null;
        }
    }

    public void deleteUserAndWidgetData(String userID) {
        delete(userID);
        widgetDataService.deleteAll(userID);
    }
}
