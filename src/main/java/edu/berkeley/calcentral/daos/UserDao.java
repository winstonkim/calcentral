package edu.berkeley.calcentral.daos;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.berkeley.calcentral.domain.CalCentralUser;

public class UserDao {

	private JdbcTemplate jdbcTemplate;

	public CalCentralUser getUser(String uid) {
//		CalCentralUser user = this.jdbcTemplate.queryForObject(
//		        "  SELECT bu_staff_id staffID, "
//		                + "  bu_last_name lastName," 
//		                + "  bu_first_name firstName, "
//		                + "  bu_user_flg userFlag, "
//		                + "  bu_email_id email, "
//		                + "  bu_dept_id dept, "
//		                + "  bu_title userTitle, "
//		                + "  bu_comments comments, "
//		                + "  bu_active_flg activeFlag, "
//		                + "  to_char(bu_modified_date, 'FMMon ddth, YYYY') modifyDate, "
//		                + "  bu_calnet_id calnetID, "
//		                + "  bu_calnet_uid "
//		                + " FROM JAZZEE.BEBOP_USERS_T "
//		                + " WHERE bu_calnet_uid = ?", new Object[] { uid },
//		                new RowMapper<CalCentralUser>() {
//					public CalCentralUser mapRow(ResultSet rs, int rowNum)
//							throws SQLException {
//						CalCentralUser user = new CalCentralUser();
//						user.setUid(rs.getString("bu_calnet_uid"));
//						user.setStaffId(rs.getString("staffID"));
//						user.setLastName(rs.getString("lastName"));
//						user.setFirstName(rs.getString("firstName"));
//						user.setUserFlag(rs.getString("userFlag"));
//                        user.setEmail(rs.getString("email"));
//						user.setDepartment(rs.getString("dept"));
//						user.setTitle(rs.getString("userTitle"));
//						user.setComments(rs.getString("comments"));
//						user.setActiveFlag(rs.getString("activeFlag"));
//						user.setModifyDate(rs.getString("modifyDate"));
//						user.setCalnetId(rs.getString("calnetID"));
//						return user;
//					}
//				});
		CalCentralUser user = new CalCentralUser();
		user.setCalnetId(uid);
		
		return user;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
}
