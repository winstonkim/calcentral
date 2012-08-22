package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.daos.UserDao;
import edu.berkeley.calcentral.domain.CalCentralUser;

public class CalCentralUserService {

    private UserDao userDao;
    
    public CalCentralUser getUser(String uid) {        
        return userDao.getUser(uid);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
