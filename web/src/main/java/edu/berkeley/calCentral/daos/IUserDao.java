package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.domain.CalCentralUser;

public interface IUserDao {
    public CalCentralUser getUser(String uid);
}
