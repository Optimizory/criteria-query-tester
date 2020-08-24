package com.otpl.criteriatester;

import com.optimizory.rmsis.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.*;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.JoinType;
import org.springframework.orm.hibernate4.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CriteriaTester {

    public static SessionFactory sessionFactory = buildSessionFactory();
    public HibernateTemplate hibernateTemplate;

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
        reqIds.add(1799L);
        reqIds.add(4011L);
        List<RequirementDependency> result = criteriaTester.criteriaBuilder(reqIds);
        System.out.println(result);
        session.getTransaction().commit();
        CriteriaTester.shutdown();
    }



    public List<RequirementDependency> criteriaBuilder(List<Long> requirementIds){

        if(requirementIds != null && !requirementIds.isEmpty()) {

            DetachedCriteria subquery = DetachedCriteria.forClass(RequirementDependency.class)
                    .createAlias("dependency", "r")
                    .add(Restrictions.in("requirementId", requirementIds))
                    .setProjection(Projections.projectionList()
                            .add(Projections.max("r.version"), "rv")
                            .add(Projections.groupProperty("requirementId"), "ri")
                            .add(Projections.groupProperty("r.requirementKey"), "rk"));

            return

                    getSessionFactory().openSession()
                            .createCriteria(RequirementDependency.class)
                            .createAlias("requirement", "r")
                            .add(Restrictions.eqProperty("r.id", "requirementId"))
                            .add(Subqueries.propertyEq("r.version", subquery))
                            .setProjection(Projections.property("id"))
                            .list();

        }
        return new ArrayList<RequirementDependency>();
    }

    /*

    HELPER STUBS

    public {
        DetachedCriteria subquery = DetachedCriteria.forClass(RequirementDependency.class)
                .createAlias("dependency", "r")
                .add(Restrictions.in("requirementId", requirementIds))
                .setProjection(Projections.projectionList()
                        .add(Projections.max("r.version"))

                        .add(Projections.groupProperty("requirementId"))
                        .add(Projections.groupProperty("r.requirementKey")));


        return
                getSessionFactory().getCurrentSession()
                        .createCriteria(RequirementDependency.class)
                        .createAlias("requirement.project", "pr")
                        //.add(SQLRestrictions.in("requirementId", requirementIds))
                        .add(Subqueries.propertyEq("version", subquery))
                        .add(Restrictions.eq("remove", false))
                        .add(Restrictions.eq("r.remove", false))
                        .add(Restrictions.eq("pr.remove", false))
                        .list();
    }

        Native SQL

        createSQLQuery("select max(r.version_number) as vm, r.requirement_key as rk, rd.dependency_id as ri from requirement_dependency as rd join requirement as r on\n" +
                                    "rd.requirement_id=r.id\n" +
                                    "group by r.requirement_key, rd.dependency_id")
                            .addEntity(RequirementDependency.class)
                            .addEntity(Requirement.class)

                         */
}
