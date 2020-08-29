package com.otpl.criteriatester;

import com.optimizory.rmsis.model.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate4.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

public class CriteriaTester {

    public static SessionFactory sessionFactory = buildSessionFactory();
    public static HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);

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
    public static HibernateTemplate getHibernateTemplate() { return hibernateTemplate; }

    public static void shutdown(){
        getSessionFactory().close();
    }

    public static void main(String[] args) {

        Session session = CriteriaTester.getSessionFactory().openSession();
        session.beginTransaction();

        CriteriaTester criteriaTester = new CriteriaTester();

        List<Object> reqIds = new ArrayList<Object>();
        reqIds.add(1799L);
        reqIds.add(4011L);
        List<RequirementDependency> result = criteriaTester.criteriaBuilder(reqIds);
        System.out.println(result);
        session.getTransaction().commit();
        CriteriaTester.shutdown();
    }



    public List<RequirementDependency> criteriaBuilder(List<Object> requirementIds){

        if(requirementIds != null && !requirementIds.isEmpty()) {

            String subquery = "(select max(r.version) from RequirementDependency as rd join rd.requirement as r " +
                    " group by r.requirementKey, rd.dependencyId having r.requirementKey = req.requirementKey and rd.dependencyId = reqd.dependencyId)";
            String query = "select reqd from RequirementDependency as reqd join reqd.requirement as req where req.version = " + subquery;

            Query subq = sessionFactory.openSession().createQuery(query);
            return subq.list();

        }
        return new ArrayList<RequirementDependency>();
    }

    /*

    HELPER STUBS :

    Criteria Query

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
