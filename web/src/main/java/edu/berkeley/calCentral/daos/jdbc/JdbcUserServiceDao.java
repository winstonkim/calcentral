/**
 * JdbcUserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos.jdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import edu.berkeley.calcentral.daos.IUserAuthorizationDao;

public class JdbcUserServiceDao implements IUserAuthorizationDao {

	private DataSource dataSource;
	
	public User getUserDetails(String uid) throws UsernameNotFoundException {
	    MapSqlParameterSource params = new MapSqlParameterSource();

	    //sanity check
	    if (uid == null || uid.isEmpty()) {
	        throw new UsernameNotFoundException("User: " + uid + " not found!");
	    }
	    params.addValue("calnetUID", uid);
	    NamedParameterJdbcTemplate paramedQueryRunner = new NamedParameterJdbcTemplate(dataSource);

	    //fetching authorities
	    String sql = SqlQueries.userPageRoles;
	    List<String> roles = paramedQueryRunner.queryForList(sql, params, String.class);
	    List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
	    for (String role : roles) {
	        authorities.add(new SimpleGrantedAuthority(role));
	    }

	    //fetching the other details
	    String moreSql = SqlQueries.selectUserAndActiveness;
	    Map<String, Object> results = paramedQueryRunner.queryForMap(moreSql, params);
	    String username = (String) results.get("username");
	    username = Strings.nullToEmpty(username);
	    String password = (String) results.get("password");
	    password = Strings.nullToEmpty(password);
	    boolean active = false;
	    if (results != null 
	            && results.get("active") != null
	            && ((String) results.get("active")).equalsIgnoreCase("Y")) {
	        active = true;
	    }
	    
	    //sanity check
	    if (!username.isEmpty()
	            && !password.isEmpty()) {
	        User populatedUser = new User(username, password, 
	                active, active, active, active, authorities);
	        return populatedUser;
	    } else {
	        throw new UsernameNotFoundException("User: " + uid + " not found!");
	    }
    }

    public boolean isSysUser(String uid) {
	    MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("calnetUID", uid);
        NamedParameterJdbcTemplate paramedQueryRunner = new NamedParameterJdbcTemplate(dataSource);
        
        String result = paramedQueryRunner.queryForObject(SqlQueries.isSysUser, params, String.class);
        if (result != null && result.equalsIgnoreCase("1")) {
            return true;
        }
        return false;
    }
	
	public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	private static class SqlQueries {
	    private static String isSysUser = 
	            "   SELECT to_char(rownum) "
	            + " FROM jazzee.BEBOP_USERS_T bu "
	            + " JOIN jazzee.BEBOP_AUTHZ_T authz "
                + "   on ((authz.ba_bebop_user_seq_id = bu.bu_staff_id) "
                + "       AND (authz.ba_active = 'Y')) "
                + " JOIN jazzee.BEBOP_ROLES_T beboproles "
                + "  on (beboproles.br_name = authz.ba_role) "
	            + " WHERE "
	            + "   bu.bu_calnet_uid = :calnetUID"
	            + "   AND beboproles.br_name = 'SYS' "
	            + "   AND authz.BA_PROGRAM_CODE = 0"
	            + "   AND beboproles.BR_ACTIVE = 'Y'"
	            + "   AND bu.bu_active_flg = 'Y'";
	    
	    private static String selectUserAndActiveness = 
	            "   SELECT to_char(bu.bu_calnet_uid) username, "
	            + "  '1' password, "
	            + "  bu.bu_active_flg active "
	            + " FROM jazzee.BEBOP_USERS_T bu "
	            + " WHERE "
	            + "   bu.bu_calnet_uid = :calnetUID";
	    
	    private static String userPageRoles =
	            "   SELECT concat('ROLE_', bp.bp_role) role "
	            + " FROM jazzee.BEBOP_USERS_T bu "
	            + " JOIN jazzee.BEBOP_AUTHZ_T authz "
	            + "   on ((authz.ba_bebop_user_seq_id = bu.bu_staff_id) "
	            + "       AND (authz.ba_active = 'Y')) "
	            + " JOIN jazzee.BEBOP_ROLES_T beboproles "
	            + "  on (beboproles.br_name = authz.ba_role) "
	            + " JOIN jazzee.BEBOP_PERMISSIONS_T permissions "
	            + "  on (beboproles.br_seq_id = permissions.bz_bebop_role_seq_id) "
	            + " JOIN jazzee.BEBOP_PAGES_T bp"
	            + "  on (bp.bp_seq_id = permissions.bz_bebop_page_seq_id) "
	            + " WHERE "
	            + "   bu.bu_calnet_uid = :calnetUID"
	            + " GROUP by bp.bp_role "
	            + " ORDER by bp_role ";
	            
	}

    
}
