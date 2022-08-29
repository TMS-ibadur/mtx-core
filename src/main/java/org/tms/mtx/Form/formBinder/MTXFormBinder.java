/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tms.mtx.Form.formBinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.springframework.beans.BeansException;
import org.tms.mtx.model.Project.ProjectAssignee;
import org.tms.mtx.model.Project.ProjectStatus;
import org.tms.mtx.model.Project.ProjectType;

/**
 *
 * @author Fawad Khaliq <khaliq@opendynamics.com.my>
 */
public class MTXFormBinder extends WorkflowFormBinder {

    public static class ActionCode {

        public static final String PROJECT_UPDATE = "PROJECT_UPDATE";
        public static final String PROJECT_ASSIGN = "PROJECT_ASSIGN";
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        try {

            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

            String code = rows.get(0).getProperty("code");
            if (code != null) {

                try (Connection con = ds.getConnection()) {
                    switch (code.toUpperCase()) {

                        case ActionCode.PROJECT_UPDATE:
                            action(element, rows, formData, con);
                            break;
                        case ActionCode.PROJECT_ASSIGN:
                            assign(element, rows, formData, con);
                            break;

                        default:
                            formData.addFormError(getFormId(), "code is out of option");
                            break;
                    }
                }
            }

        } catch (SQLException | BeansException ex) {
            Logger.getLogger(MTXFormBinder.class.getName()).log(Level.SEVERE, null, ex);
            formData.addFormError(getFormId(), ex.getMessage());
        }

        return rows;
    }

    public FormRowSet action(Element element, FormRowSet rows, FormData formData, Connection con) throws SQLException {
        System.out.println("PROJECT_UPDATE");
        FormRow row = rows.get(0);

        String IS_SAVE_AS_DRAFT = formData.getRequestParameter("IS_SAVE_AS_DRAFT") != null
                ? formData.getRequestParameter("IS_SAVE_AS_DRAFT")
                : "";
        if (IS_SAVE_AS_DRAFT.equals("Yes")) {
            row.setProperty("projectStatus", ProjectStatus.DRAFT);
            super.store(element, rows, formData);
            return rows;
        }

        String projectStatus = row.getProperty("projectStatus") != null ? row.getProperty("projectStatus") : "";

        if (projectStatus.equalsIgnoreCase(ProjectStatus.DRAFT)
                || projectStatus.equalsIgnoreCase(ProjectStatus.REJECTED)) {

            row.setProperty("projectStatus", ProjectStatus.PENDING_APPROVAL);
            super.store(element, rows, formData); // Update
            return rows;

        } else if (projectStatus.equalsIgnoreCase(ProjectStatus.ASSIGNED)) {

            assign(element, rows, formData, con);
            return rows;

        } else {
            super.store(element, rows, formData);
        }

        return rows;
    }

    public FormRowSet assign(Element element, FormRowSet rows, FormData formData, Connection con) throws SQLException {
        FormRow row = rows.get(0);
        String projectStatus = row.getProperty("projectStatus") != null ? row.getProperty("projectStatus") : "";
        super.store(element, rows, formData);
        // ############### check assignment start ###############################
        String selRnD = "";
        String selRegulatory = "";
        String selSample = "";
        String selApplication = "";
        String selCS = "";
        String assignee = "";
        String projectType = "";
        int totalAssignee = 0;

        // getting assignee from form data
        String sql = "SELECT * FROM app_fd_crm_proj_management WHERE id = '" + formData.getPrimaryKeyValue() + "'";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            ResultSet rSet = stmt.executeQuery();
            if (rSet.next()) {
                assignee = rSet.getString("c_assignment") != null ? rSet.getString("c_assignment") : "";
                selRnD = rSet.getString("c_selRnD") != null ? rSet.getString("c_selRnD") : "";
                selRegulatory = rSet.getString("c_selRegulatory") != null ? rSet.getString("c_selRegulatory") : "";
                selSample = rSet.getString("c_selSample") != null ? rSet.getString("c_selSample") : "";
                selApplication = rSet.getString("c_selApplication") != null ? rSet.getString("c_selApplication") : "";
                selCS = rSet.getString("c_selCS") != null ? rSet.getString("c_selCS") : "";
                projectType = rSet.getString("c_projectType") != null ? rSet.getString("c_projectType") : "";
                projectStatus = rSet.getString("c_projectStatus") != null ? rSet.getString("c_projectStatus") : "";
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

        if (projectType.equalsIgnoreCase(ProjectType.CUST_LIB) || projectType.equalsIgnoreCase(ProjectType.SHOWCASE)) {

            if (projectStatus.equalsIgnoreCase(ProjectStatus.OPENED)
                    || projectStatus.equalsIgnoreCase(ProjectStatus.ASSIGNED)) {
                if (assignee.contains(ProjectAssignee.APPLICATION)) {
                    if (!selApplication.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.REGULATORY)) {
                    if (!selRegulatory.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.SAMPLETEAM)) {
                    if (!selSample.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.RND)) {
                    if (!selRnD.isEmpty()) {
                        totalAssignee++;
                    }
                }

                // check if all assignee is selected then update status accordingly
                /*
                 * compare totalAssignee with total assignee for this project
                 * assignee.length() - 1 because CS Team are mandatory assignee
                 */
                if (totalAssignee == assignee.split(";").length - 1) {
                    row.setProperty("projectStatus", ProjectStatus.ASSIGNED);
                    super.store(element, rows, formData);// Update
                } else {
                    row.setProperty("projectStatus", ProjectStatus.OPENED);
                    super.store(element, rows, formData);// Update
                }
                // ############### check assignment end ###############################

            }

        } else {

            if (projectStatus.equalsIgnoreCase(ProjectStatus.OPENED)
                    || projectStatus.equalsIgnoreCase(ProjectStatus.ASSIGNED)) {

                if (assignee.contains(ProjectAssignee.APPLICATION)) {
                    if (!selApplication.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.REGULATORY)) {
                    if (!selRegulatory.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.SAMPLETEAM)) {
                    if (!selSample.isEmpty()) {
                        totalAssignee++;
                    }
                }
                if (assignee.contains(ProjectAssignee.CSTEAM)) {
                    if (!selCS.isEmpty()) {
                        totalAssignee++;
                    }
                }

                // check if all assignee is selected then update status accordingly
                /*
                 * compare totalAssignee with total assignee for this project
                 * assignee.length() - 2 because R&D and Approver are mandatory assignee
                 */
                if (totalAssignee == assignee.split(";").length - 2) {
                    row.setProperty("projectStatus", ProjectStatus.ASSIGNED);
                    super.store(element, rows, formData);// Update
                } else {
                    row.setProperty("projectStatus", ProjectStatus.OPENED);
                    super.store(element, rows, formData);// Update
                }

                // ############### check assignment end ###############################

            }
        }

        super.store(element, rows, formData);

        return rows;
    }

    @Override
    public String getName() {
        return ("MTX Form Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("MTX Form Binder");
    }

    @Override
    public String getLabel() {
        return ("MTX Form Binder");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormId() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_ID);
    }

    @Override
    public String getTableName() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
    }
}
