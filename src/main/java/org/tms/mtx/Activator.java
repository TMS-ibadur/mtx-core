package org.tms.mtx;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.tms.mtx.Datalist.datalistAction.projectEdit;
import org.tms.mtx.Form.formBinder.MTXFormBinder;
import org.tms.mtx.Form.formPermission.ProjectAssignment;
import org.tms.mtx.Section.SectionBinder.AuditTrail;
import org.tms.mtx.ws.webservices;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    @Override
    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        //registrationList.add(context.registerService(MyPlugin.class.getName(), new MyPlugin(), null));
        
        registrationList.add(context.registerService(MTXFormBinder.class.getName(), new MTXFormBinder(), null));
        registrationList.add(context.registerService(webservices.class.getName(), new webservices(), null));

        //permission plugin
        registrationList.add(context.registerService(ProjectAssignment.class.getName(), new ProjectAssignment(), null));
        
        //datalist action
        registrationList.add(context.registerService(projectEdit.class.getName(), new projectEdit(), null));

        //section plugin
        registrationList.add(context.registerService(AuditTrail.class.getName(), new AuditTrail(), null));
    }

    @Override
    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}