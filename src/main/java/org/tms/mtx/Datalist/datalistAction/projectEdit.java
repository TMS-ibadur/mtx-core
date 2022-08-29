package org.tms.mtx.Datalist.datalistAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class projectEdit extends DataListActionDefault {
    private final static String MESSAGE_PATH = "messages/hyperlinkDataListAction";

    @Override
    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        // only allow POST
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        // check for submited rows
        System.out.println(rowKeys);
        if (rowKeys != null && rowKeys.length > 0) {
            try {
                // get the HTTP Response
                HttpServletResponse response = WorkflowUtil.getHttpServletResponse();
                if (rowKeys.length == 1) {
                    statusCheck(request, response, rowKeys[0]);
                }
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "Project Datalist Action : " + ArrayUtils.toString(rowKeys));
            }
        }
        return null;
    }

    private void statusCheck(HttpServletRequest request, HttpServletResponse response, String string) {
        System.out.println(string);
    }

    @Override
    public String getConfirmation() {

        return getPropertyString("confirmation");
    }

    @Override
    public String getHref() {
        
        return "";
    }

    @Override
    public String getHrefColumn() {
        return getPropertyString("hrefColumn");
    }

    @Override
    public String getHrefParam() {
        return getPropertyString("hrefParam");
    }

    @Override
    public String getLinkLabel() {
        return getPropertyString("label");
    }

    @Override
    public String getTarget() {
        return getPropertyString("target");
    }

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

        return AppUtil.readPluginResource(getClassName(), "/properties/datalist/hyperlinkDataListAction.json", null, true,
                MESSAGE_PATH);
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public String getName() {

        return "Project Edit Datalist Action";
    }

    @Override
    public String getVersion() {

        return "1.0";
    }

}
