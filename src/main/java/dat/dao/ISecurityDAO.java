package dat.dao;

import dat.enums.Roles;
import dk.bugelhartmann.UserDTO;
import dat.entities.Account;
import dat.exceptions.ValidationException;


public interface ISecurityDAO
{
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    Account createUser(String username, String password);
    Account addRoleToUser(String username, Roles role);
    Account removeRoleFromUser(String username, Roles role);
}
