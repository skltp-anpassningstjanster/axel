package se.inera.axel.riv.authentication.impl;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.Sha1CredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import se.inera.axel.riv.authentication.LoginService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LoginServiceImpl implements LoginService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private DriverManagerDataSource dataSource;

    @Autowired
    private Sha1CredentialsMatcher matcher;

    @Override
    public boolean authenticate(String username, String password) {
        String hash;
        try {
            hash = getPasswordHashForUser(username);
        } catch (SQLException e) {
            log.error("Exception while connect to database. {}", e.getMessage());
            return false;
        }

        if (hash == null) {
            log.error("No user {} exists. ", username);
            return false;
        }

        boolean loggedIn = credentialsMatch(username, password, hash);
        if (!loggedIn) {
            log.error("The password is not valid for user {}.", username);
        } else {
            log.info("User {} logged in.", username);
        }
        return loggedIn;
    }

    private boolean credentialsMatch(String username, String password, String hash) {
        SimpleAccount account = new SimpleAccount(username, hash, "ShiroDbRealm");
        AuthenticationToken authenticationToken = new UsernamePasswordToken(username, password);

        return matcher.doCredentialsMatch(authenticationToken, account);
    }

    private String getPasswordHashForUser(String username) throws SQLException {
        String hash;
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * from Anvandare WHERE  anvandarnamn = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            hash = rs.getString("losenord_hash");
            return hash;
        }
        return null;
    }
}
