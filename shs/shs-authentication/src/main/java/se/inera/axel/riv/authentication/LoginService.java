package se.inera.axel.riv.authentication;

import org.springframework.stereotype.Component;

@Component
public interface LoginService {
 
    public boolean authenticate(String username, String password);

}
