/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tms.mtx.ws;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.tms.mtx.util.Util;

/**
 *
 * @author fawadkhaliq
 */
public class ExpressionUtil {

    private Map<String, String> variablesGiven;

    private ArrayList<String> toList(String raw) {

        raw = raw != null ? raw.replaceAll(" ", "") : "";

        ArrayList<String> list = new ArrayList<>();

        String symbol = "";
        for (int i = 0; i < raw.length(); i++) {

            String ch = raw.charAt(i) + "";

            if (ch.equals("*") || ch.equals("/") || ch.equals("+") || ch.equals("-") || ch.equals("(") || ch.equals(")")) {

                if (!symbol.isEmpty()) {
                    list.add(symbol);
                }

                list.add(ch);

                symbol = "";
            } else {
                symbol += ch;
            }
        }

        if (!symbol.isEmpty()) {
            list.add(symbol);
        }

        return list;
    }

    private ArrayList<String> infixToPosfix(ArrayList<String> infix) {

        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("*", 10);
        precedence.put("/", 10);

        precedence.put("+", 9);
        precedence.put("-", 9);

        ArrayList<String> postfix = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (int i = 0; i < infix.size(); i++) {

            String symbol = infix.get(i) + "";

            if (symbol.trim().isEmpty()) {//ignore white spaces
                continue;
            }

            if (symbol.equals("(")) {
                stack.push(symbol);
            } else if (symbol.equals("*") || symbol.equals("/") || symbol.equals("+") || symbol.equals("-")) {

                while (!stack.isEmpty() && !stack.peek().equals("(") && !stack.peek().equals(")") && precedence.get(stack.peek()) >= precedence.get(symbol)) {
                    postfix.add(stack.pop());
                }
                stack.push(symbol);
            } else if (symbol.equals(")")) {

                while (!stack.isEmpty()) {
                    String pop = stack.pop();
                    if (pop.equals("(")) {
                        break;
                    } else {
                        postfix.add(pop);
                    }
                }
            } else {
                postfix.add(symbol);
            }
        }

        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    class NotFoundException extends Exception {

        public NotFoundException(String errorMessage) {
            super(errorMessage);
        }
    }

    private String getFormulaByPricingCode(String pricingCode, Connection con) throws ClassNotFoundException, SQLException, NotFoundException {

        String result;
        String sql
                = "SELECT *"
                + " FROM app_fd_crm_pricing_setup"
                + " WHERE c_pricingCode = ?";

        try ( PreparedStatement preparedStmt = con.prepareStatement(sql)) {
            preparedStmt.setString(1, pricingCode);
            try ( ResultSet rSet = preparedStmt.executeQuery()) {

                if (rSet.next()) {
                    result = rSet.getString("c_formula") != null ? rSet.getString("c_formula") : "";
                } else {
                    throw new NotFoundException("Record not found in pricing setup for pricing code (" + pricingCode + ")");
                }
            }
        }

        if (result.isEmpty()) {
            throw new NotFoundException("Formula value is empty in pricing setup for pricing code (" + pricingCode + ")");
        }

        return result;
    }

    private ArrayList<String> getListedExpression(String infixStr, Connection con) throws ClassNotFoundException, SQLException, NotFoundException {

        ArrayList<String> listedInfix = toList(infixStr);

        ArrayList<String> listedInfixNew = new ArrayList<>();
        for (String s : listedInfix) {

            if (s.equals("*") || s.equals("/") || s.equals("+") || s.equals("-") || s.equals("(") || s.equals(")")) {

                listedInfixNew.add(s);

            } else if (variablesGiven.containsKey(s)) { //ignore TP

                listedInfixNew.add(s); //and also ensure capital

            } else if (Util.isNumeric(s)) {

                listedInfixNew.add(s);

            } else {//is pricing code

                String subExpresionStr = getFormulaByPricingCode(s, con);
                ArrayList<String> subExpressionList = getListedExpression(subExpresionStr, con);

                if (!subExpressionList.isEmpty()) {
                    listedInfixNew.add("(");
                    for (String ss : subExpressionList) {
                        listedInfixNew.add(ss);
                    }
                    listedInfixNew.add(")");
                }
            }
        }

        return listedInfixNew;
    }

    private String resolvePostfix(ArrayList<String> postfix) {

        Stack<String> stack = new Stack();
        for (String s : postfix) {

            if (s.equals("*")) {

                String x = stack.pop();
                String y = stack.pop();

                Double result = Double.valueOf(x) * Double.valueOf(y);

                stack.push(result + "");
            } else if (s.equals("/")) {

                String x = stack.pop();
                String y = stack.pop();

                Double result = Double.valueOf(y) / Double.valueOf(x);

                stack.push(result + "");
            } else if (s.equals("+")) {

                String x = stack.pop();
                String y = stack.pop();

                Double result = Double.valueOf(x) + Double.valueOf(y);

                stack.push(result + "");
            } else if (s.equals("-")) {

                String x = stack.pop();
                String y = stack.pop();

                Double result = Double.valueOf(y) - Double.valueOf(x);

                stack.push(result + "");
            } else {
                stack.push(s);
            }
        }

        if (!stack.isEmpty()) {
            return stack.pop();
        }

        return "";
    }

    public String evaluate(Map<String, String> variablesGiven, String pricingCode, Connection con) throws ClassNotFoundException, SQLException, NotFoundException {

        this.variablesGiven = variablesGiven;

        String infix = getFormulaByPricingCode(pricingCode, con);

        //print(infix);
        ArrayList<String> listedInfix = getListedExpression(infix, con);

        //print(listedInfix);
        ArrayList<String> postfix = infixToPosfix(listedInfix);

        //print(postfix);
        //--[Start] Replace variables by their values---------------------------
        ArrayList<String> postfixNew = new ArrayList<>();
        for (String s : postfix) {

            if (variablesGiven.containsKey(s)) {

                postfixNew.add(variablesGiven.get(s));
            } else {
                postfixNew.add(s);
            }
        }
        //--[End] Replace variables by their values-----------------------------

        //print(postfixNew);
        String result = resolvePostfix(postfixNew);

        return result;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, NotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        try ( Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jwdb", "root", "Pl@nt4root")) {

            Map<String, String> variablesGiven = new HashMap<>();
            variablesGiven.put("TP", "5");

            ExpressionUtil e = new ExpressionUtil();
            String priceCode = "EXP01";
            String result = e.evaluate(variablesGiven, priceCode, con);
            System.out.println("Result: " + result);
        }
    }

    private void print(String s) {

        System.out.print(s);

        System.out.println("\n-------------");
    }

    private void print(ArrayList<String> list) {

        for (String s : list) {
            System.out.print(s + ",");
        }
        System.out.println("\n-------------");
    }

    /*
    public void testCase() {

        ArrayList<String> cases = new ArrayList<>();

        cases.add("(((TP / 0.7 ) / 3.2) * 0.92) * 0.82");
        cases.add("((TP / 0.7 ) / 3.2) * 0.82");
        cases.add("EXP02 * 0.92");

        cases.add("(A+B/C*(D+E)-F)");

        cases.add("A + B * C + D"); //A B C * + D +
        cases.add("(((A + ((B * C) + D))))"); //A B C * + D +

        cases.add("(A + B14) * (C + D0043)");//A B + C D + *
        cases.add("A * B + C * D");//A B * C D * +
        cases.add("14 * 10 + 5 * TA");//14 10 * 5 TA * +
        cases.add("A + B + C + D");//A B + C + D +

        String c = cases.get(5);

        print(c);
        ArrayList<String> infix = toList(c);
        print(infix);

        ArrayList<String> list = infixToPosfix(infix);
        print(list);
    }
     */
}
