package com.otpl.criteriatester;

import com.optimizory.rmsis.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import java.util.ArrayList;
import java.util.List;

public class CriteriaTester {

    public static SessionFactory sessionFactory = buildSessionFactory();

    public static SessionFactory buildSessionFactory() {

        try {
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        }
        catch (Throwable e){
            System.err.println("Failed" + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }

    public static void shutdown(){
        getSessionFactory().close();
    }

    public static void main(String[] args) {

        Session session = CriteriaTester.getSessionFactory().openSession();
        session.beginTransaction();

        CriteriaTester criteriaTester = new CriteriaTester();

        List<Long> reqIds = new ArrayList<Long>();
        reqIds.add(4010L);
        reqIds.add(4011L);
        List<RequirementDependency> result = criteriaTester.criteriaBuilder(reqIds);
        System.out.println(result);
        session.getTransaction().commit();
        CriteriaTester.shutdown();
    }



    public List<RequirementDependency> criteriaBuilder(List<Long> dependencyIds){

        if(dependencyIds != null && !dependencyIds.isEmpty()){

            return
                getSessionFactory().openSession()
                    .createCriteria(RequirementDependency.class)
                    .createAlias("requirement","r")
                    .add(Restrictions.in("requirementId", dependencyIds))
                    .add(Restrictions.eq("r.projectId",708L))
                        .setProjection(Projections.projectionList()
                        .add(Projections.property("r.version")))
                    .list();
        }
        return new ArrayList<RequirementDependency>();
    }
}
