package se.inera.axel.riv.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class LoginService {
    private ApplicationContext appContext;

//    @Autowired
//    private JpaTransactionManager transactionManager;
//
    private DriverManagerDataSource dataSource;

    public LoginService() {
    }

    @Autowired
    public LoginService(DriverManagerDataSource dataSource, ApplicationContext appContext) {
        this.appContext = appContext;
        this.dataSource = dataSource;
    }

    public boolean authenticate()  {
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT anvandarnamn FROM Anvandare");
            while (rs.next()) {
                System.out.println(rs.getString("anvandarnamn"));
            }


            return true;
        }catch (Exception e){
            System.out.println(e);
        }
        return true;
    }

}
