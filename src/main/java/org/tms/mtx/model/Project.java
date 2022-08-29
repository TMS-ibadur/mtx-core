package org.tms.mtx.model;

public class Project {

    public static class ProjectType {

        public static String CUST_LIB = "Customer Library/ Replenishment";
        public static String SHOWCASE = "Exhibition Showcase";
        public static String OPP_GAIN = "New Opp-Gain";
        public static String OPP_PROACTIVE = "New Opp-Proactive Concept";
        public static String OPP_CAPTURE_MATCHING = "New Opp-Capture Matching";
        public static String OPP_CAPTURE_NON_MATCHING = "New Opp-Capture Non Matching";
        public static String DEF_COMP = "Def-With competitor";
        public static String DEF_WO_COMP = "Def-Without competitor";

    }

    public static class ProjectStatus {

        public static String DRAFT = "Draft";
        public static String PENDING_APPROVAL = "Pending Approval";
        public static String OPENED = "Opened";
        public static String ASSIGNED = "Assigned";
        public static String APPROVED = "Approved";
        public static String REJECTED = "Closed - Rejection";

    }

    public static class ProjectAssignee {

        public static String APPLICATION = "Application";
        public static String REGULATORY = "Regulatory";
        public static String SAMPLETEAM = "Sample";
        public static String CSTEAM = "CSTeam";
        public static String RND = "RnD";

    }
    
}
