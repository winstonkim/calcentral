package edu.berkeley.calCentral.daos;

import edu.berkeley.calCentral.domain.CalCentralUser;

public interface IUserDao {
    public CalCentralUser getUser(String uid);
}
