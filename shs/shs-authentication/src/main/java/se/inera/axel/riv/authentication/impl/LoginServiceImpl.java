package se.inera.axel.riv.authentication.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import se.inera.axel.riv.authentication.LoginService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class LoginServiceImpl implements LoginService {
	
	@Autowired
    private ApplicationContext appContext;

//    @Autowired
//    private JpaTransactionManager transactionManager;
//
	@Autowired
    private DriverManagerDataSource dataSource;

    @Override
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
