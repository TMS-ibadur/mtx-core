package org.tms.mtx.Form.formPermission;

import java.sql.*;

import javax.sql.DataSource;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.tms.mtx.model.Project.ProjectAssignee;
import org.tms.mtx.model.Project.ProjectStatus;
import org.tms.mtx.model.Project.ProjectType;

public class ProjectAssignment extends UserviewPermission implements FormPermission {

    @Override
    public String getClassName() {

        return getClass().getName();
    }

    @Override
    public String getLabel() {

        return null;
    }

    @Override
    public String getPropertyOptions() {

        return null;
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public String getName() {

        return "Project Assignment Permission";
    }

    @Override
    public String getVersion() {

        return "1.0";
    }

    @Override
    public boolean isAuthorize() {
        // Permission Check for User that is involved in the project
        String projectId = getRequestParameterString("id");
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        String sql = "select userId, group_concat(groupId) as groupId, c_assignment, p.c_projectStatus, p.c_projectType "
                + "from dir_user_group g "
                + "inner join app_fd_crm_proj_management p on (p.c_assignment like concat('%', g.groupId, '%')) "
                + "where p.id = ? and g.userId = ?  and p.c_projectStatus in (?,?,?,?) "
                + "group by userId";
        try {
            con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, projectId);
            stmt.setString(2, getCurrentUser().getId());
            stmt.setString(3, ProjectStatus.PENDING_APPROVAL);
            stmt.setString(4, ProjectStatus.APPROVED);
            stmt.setString(5, ProjectStatus.OPENED);
            stmt.setString(6, ProjectStatus.ASSIGNED);
            ResultSet rs = stmt.executeQuery();
            // group an array of tasks by category
            while (rs.next()) {
                if (rs.getString("c_projectType").equalsIgnoreCase(ProjectType.CUST_LIB)
                        || rs.getString("c_projectType").equalsIgnoreCase(ProjectType.SHOWCASE)) {
                    if (rs.getString("c_projectStatus").equalsIgnoreCase(ProjectStatus.PENDING_APPROVAL)) {

                        if (rs.getString("groupId").contains(ProjectAssignee.CSTEAM)) {
                            return true;
                        }

                    } else {
                        return true;
                    }
                } else {
                    if (rs.getString("c_projectStatus").equalsIgnoreCase(ProjectStatus.APPROVED)) {

                        if (rs.getString("groupId").contains(ProjectAssignee.RND)) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

}
