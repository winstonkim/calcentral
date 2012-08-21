package edu.berkeley.calCentral.services;

import edu.berkeley.calCentral.daos.IUserDao;
import edu.berkeley.calCentral.domain.CalCentralUser;

public class BebopUserService {

    private IUserDao userDao;
    
    public CalCentralUser getUser(String uid) {        
        return userDao.getUser(uid);
    }

    public void setUserDao(IUserDao userDao) {
        this.userDao = userDao;
    }
}
