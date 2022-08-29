package org.tms.mtx.Section.SectionBinder.AuditTrailProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;

public class SampleAudit {

    public static FormRowSet audit(Element element, FormRowSet rows, FormData formData, Connection con)
            throws SQLException, Exception {

        FormRow row = rows.get(0);

        // only audit when there is a change
        if (row.getProperty("changes") != null && !row.getProperty("changes").trim().isEmpty()) {

            String sql = "INSERT INTO app_fd_crm_sample_audit "
                    + "(id, c_UpdatedDate, c_UpdatedName, c_changes, c_sampleId) "
                    + "VALUES (uuid(), ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, row.getProperty("UpdatedDate"));
            ps.setString(2, row.getProperty("UpdatedName"));
            ps.setString(3, row.getProperty("changes"));
            ps.setString(4, formData.getPrimaryKeyValue());
            ps.executeUpdate();

        }

        return rows;
    }
}
