package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.daos.IUserDao;
import edu.berkeley.calcentral.domain.CalCentralUser;

public class CalCentralUserService {

    private IUserDao userDao;
    
    public CalCentralUser getUser(String uid) {        
        return userDao.getUser(uid);
    }

    public void setUserDao(IUserDao userDao) {
        this.userDao = userDao;
    }
}
