/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tms.mtx.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONException;
import org.json.JSONObject;
import org.tms.mtx.util.Util;
import org.tms.mtx.ws.ExpressionUtil.NotFoundException;

import javax.sql.DataSource;
import org.json.JSONArray;

/**
 *
 * @author Fawad Khaliq <khaliq@opendynamics.com.my>
 */
public class webservices extends DefaultPlugin implements PluginWebSupport {

    @Override
    public String getName() {
        return "MTX WebServices";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "MTX WebServices";
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null; // not relevant
    }

    @Override
    public Object execute(Map properties) {
        return null; // not relevant
    }

    public String getLabel() {
        return "MTX WebServices";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DataSource dataSource = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        try (Connection con = dataSource.getConnection()) {
            String method = request.getParameter("method");

            if (method == null || method.isEmpty()) {
                response.sendError(403, "Parameter method is missing or has no value");
                return;
            } else {
                switch (method.toUpperCase()) {

                    case "GET_ALL_PRODUCTS":
                        getAllProducts(request, response, con);
                        break;

                    case "GET_PRODUCT_REQUIRMENTS":
                        getProductRequirments(request, response, con);
                        break;

                    case "CALCULATE_PRICE":
                        calculatePrice(request, response, con);
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(webservices.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void calculatePrice(HttpServletRequest request, HttpServletResponse response, Connection con)
            throws SQLException, JSONException, IOException, ClassNotFoundException {

        String productId = request.getParameter("productId") != null ? request.getParameter("productId") : "";
        String customerId = request.getParameter("customerId") != null ? request.getParameter("customerId") : "";

        if (productId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Parameter productId is empty");
            Logger.getLogger(webservices.class.getName()).log(Level.INFO, "Parameter productId is empty");
            return;
        }

        if (customerId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Parameter customerId is empty");
            Logger.getLogger(webservices.class.getName()).log(Level.INFO, "Parameter customerId is empty");
            return;
        }

        String sql = "SELECT *"
                + " FROM app_fd_crm_prod_info"
                + " WHERE id = ?";

        String c_transferPrice;
        String c_productType;
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rSet = stmt.executeQuery()) {
                if (rSet.next()) {
                    c_transferPrice = rSet.getString("c_transferPrice") != null ? rSet.getString("c_transferPrice")
                            : "";
                    c_productType = rSet.getString("c_productType") != null ? rSet.getString("c_productType") : "";
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Product Info record not found for selected product code");
                    Logger.getLogger(webservices.class.getName()).log(Level.INFO,
                            "Product Info record not found for selected product code (" + productId + ")");
                    return;
                }
            }
        }

        if (c_transferPrice.isEmpty() || !Util.isNumeric(c_transferPrice)) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
                    "Transfer price value in product info's record is either emtpy or non-numeric");
            Logger.getLogger(webservices.class.getName()).log(Level.INFO,
                    "Transfer price value in product info's record is either emtpy or non-numeric");
            return;
        }

        if (c_productType.isEmpty()) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
                    "Product type value in product info's record is emtpy");
            Logger.getLogger(webservices.class.getName()).log(Level.INFO,
                    "Product type value in product info's record is emtpy");
            return;
        }

        sql = "SELECT *"
                + " FROM app_fd_crm_pscheme"
                + " WHERE c_fk = ?"
                + " AND (c_product = ? OR c_product = ?)";

        String c_pricingScheme;
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            stmt.setString(2, "ALL");
            stmt.setString(3, c_productType);

            try (ResultSet rSet = stmt.executeQuery()) {
                if (rSet.next()) {
                    c_pricingScheme = rSet.getString("c_pricingScheme") != null ? rSet.getString("c_pricingScheme")
                            : "";
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer's (" + customerId
                            + ") pricing scheme record not found for product type (" + c_productType + " | ALL)");
                    Logger.getLogger(webservices.class.getName()).log(Level.INFO, "Customer's (" + customerId
                            + ") pricing scheme record not found for product type (" + c_productType + " | ALL)");
                    return;
                }
            }
        }

        if (c_pricingScheme.isEmpty()) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Pricing Scheme value is empty for customer ("
                    + customerId + ") in pricing scheme record for product type (" + c_productType + " | ALL)");
            Logger.getLogger(webservices.class.getName()).log(Level.INFO, "Pricing Scheme value is empty for customer ("
                    + customerId + ") in pricing scheme record for product type (" + c_productType + " | ALL)");
            return;
        }

        Map<String, String> variablesGiven = new HashMap<>();
        variablesGiven.put("TP", c_transferPrice);

        ExpressionUtil e = new ExpressionUtil();

        String result;
        try {
            result = e.evaluate(variablesGiven, c_pricingScheme, con);
        } catch (NotFoundException nex) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, nex.getMessage());
            Logger.getLogger(webservices.class.getName()).log(Level.INFO, nex.getMessage());
            return;
        }

        if (Util.isNumeric(result)) {
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(Double.valueOf(result));
        }

        JSONObject ob = new JSONObject();

        ob.put("result", result);

        try (PrintWriter out = response.getWriter()) {
            out.print(ob);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public void getProductRequirments(HttpServletRequest request, HttpServletResponse response, Connection con)
            throws SQLException, JSONException, IOException {

        String id = request.getParameter("id");

        String sql = " SELECT a.id,"
                + " GROUP_CONCAT(' ', a.c_productAttribute) AS 'req'"
                + " FROM app_fd_crm_prod_info i"
                + " INNER JOIN app_fd_crm_prod_attr a ON i.id= a.c_fk WHERE i.id = '" + id + "'"
                + " AND a.c_includeDoc = 'Yes'"
                + " GROUP BY i.id";

        JSONObject ob = new JSONObject();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            ResultSet rSet = stmt.executeQuery();
            if (rSet.next()) {
                ob.put("req", rSet.getString("req") != null ? rSet.getString("req") : "");
            }
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(ob);
            response.setStatus(200);
        }
    }

    public void getAllProducts(HttpServletRequest request, HttpServletResponse response, Connection con)
            throws SQLException, JSONException, IOException {

        JSONArray array = new JSONArray();
        String sql = " SELECT"
                + " p.id,"
                + " p.c_productCode,"
                + " p.c_productName,"
                + " p.c_productType"
                + " FROM app_fd_crm_prod_info p";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            ResultSet rSet = stmt.executeQuery();
            while (rSet.next()) {
                JSONObject ob = new JSONObject();
                ob.put("id", rSet.getString("id"));
                ob.put("productCode", rSet.getString("c_productCode") != null ? rSet.getString("c_productCode") : "");
                ob.put("productName", rSet.getString("c_productName") != null ? rSet.getString("c_productName") : "");
                ob.put("productType", rSet.getString("c_productType") != null ? rSet.getString("c_productType") : "");

                array.put(ob);
            }
        }

        try (PrintWriter out = response.getWriter()) {
            out.println(array.toString());
            response.setStatus(200);
        }
    }

}
