package edu.berkeley.calcentral.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.domain.CalCentralUser;

@Service
public class UserDataService {
    
    @Autowired
    private UserDataDao userDataDao;
    
    public CalCentralUser get(String uid) {
        return userDataDao.get(uid);
    }
    
    public void delete(String uid) {
        userDataDao.delete(uid);
    }
    
    public void update(String uid) {
        userDataDao.update(uid);
    }
}
