package org.tms.mtx.Section.SectionBinder;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.tms.mtx.Section.SectionBinder.AuditTrailProvider.ProjectAudit;
import org.tms.mtx.Section.SectionBinder.AuditTrailProvider.QuotationAudit;
import org.tms.mtx.Section.SectionBinder.AuditTrailProvider.SampleAudit;

public class AuditTrail extends WorkflowFormBinder {

    public static class AuditCode {

        // USER MANAGEMENT RELATED AUDIT TRAILS
        public static final String USER = "USER";
        public static final String GROUP = "GROUP";

        // CUSTOMER RELATED AUDIT TRAILS
        public static final String CUSTOMER = "CUSTOMER";
        public static final String CONTACT = "CONTACT";
        
        // PROJECT RELATED AUDIT TRAILS
        public static final String PROJECT = "PROJECT";
        public static final String SAMPLE = "SAMPLE";
        public static final String SAMPLE_PRODUCT = "SAMPLE_PRODUCT";
        // FOR BOTH STANDALONE AND PROJECT
        public static final String QUOTATION = "QUOTATION";
        public static final String QUOTATION_PRODUCT = "QUOTATION_PRODUCT";
        
        // ETC
        public static final String PRODUCT = "PRODUCT";
        public static final String PROTOTYPE = "PROTOTYPE";
        public static final String RECIPE = "RECIPE";
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        try {

            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

            String auditCode = rows.get(0).getProperty("auditCode");
            if (auditCode != null) {

                try (Connection con = ds.getConnection()) {
                    switch (auditCode) {
                        case AuditCode.USER:

                            break;

                        case AuditCode.GROUP:

                            break;

                        case AuditCode.CUSTOMER:

                            break;

                        case AuditCode.CONTACT:

                            break;

                        case AuditCode.PRODUCT:

                            break;

                        case AuditCode.PROTOTYPE:

                            break;

                        case AuditCode.RECIPE:

                            break;

                        case AuditCode.PROJECT:
                            ProjectAudit.audit(element, rows, formData, con);
                            break;

                        case AuditCode.SAMPLE:
                            SampleAudit.audit(element, rows, formData, con);
                            break;

                        case AuditCode.SAMPLE_PRODUCT:

                            break;

                        case AuditCode.QUOTATION:
                            QuotationAudit.audit(element, rows, formData, con);
                            break;

                        case AuditCode.QUOTATION_PRODUCT:

                            break;

                        default:
                            break;
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AuditTrail.class.getName()).log(Level.SEVERE, null, ex);
            formData.addFormError(getFormId(), ex.getMessage());
        }

        return rows;
    }

    @Override
    public String getName() {
        return ("Audit Trail Form Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("Audit Trail Form Binder");
    }

    @Override
    public String getLabel() {
        return ("Audit Trail Form Binder");
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
