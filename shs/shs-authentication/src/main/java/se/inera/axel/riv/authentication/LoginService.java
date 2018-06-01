package se.inera.axel.riv.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Driver;

@Component
public class LoginService {
    public LoginService() {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/spring-context.xml");
        DriverManagerDataSource dataSource = (DriverManagerDataSource)context.getBean("dataSource");
    }
//
//    @Autowired
//    private JpaTransactionManager transactionManager;
//
//    @Autowired
//    private DriverManagerDataSource dataSource;

    public boolean authenticate()  {
//        Connection conn = dataSource.getConnection();
//        Statement stmt = conn.createStatement();
//         ResultSet rs = stmt.executeQuery("SELECT NAME FROM TEST");
//        while (rs.next()) {
//            System.out.println(rs.getString("name"));
//        }



        return true;
    }

}
