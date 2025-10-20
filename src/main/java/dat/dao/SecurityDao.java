package dat.dao;

import java.util.stream.Collectors;

import dk.bugelhartmann.UserDTO;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.entities.Account;
import dat.enums.Roles;
import dat.exceptions.DaoException;
import dat.exceptions.ValidationException;

public class SecurityDao {

    private static SecurityDao instance;
    private static EntityManagerFactory emf;

    private final Logger logger = LoggerFactory.getLogger(SecurityDao.class);

    private SecurityDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static SecurityDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SecurityDao(emf);
        }
        return instance;
    }


    // TODO: Method from GenericDao.java. Should be integrated properly.
    public Account create(Account account) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(account);
            em.getTransaction().commit();
            return account;
        } catch (Exception e) {
            logger.error("Error persisting object to db", e);
            throw new DaoException("Error persisting object to db. ", e);
        }
    }

//    // TODO: Method from GenericDao.java. Should be integrated properly.
//    public Account getById(int id) {
//        try (EntityManager em = emf.createEntityManager()) {
//            Account account = em.find(Account.class, id);
//            if (account == null) {
//                throw new EntityNotFoundException("No entity found with id " + id);
//            }
//            return account;
//        } catch (Exception e) {
//            logger.error("Error reading object from db", e);
//            throw new DaoException("Error reading object from db", e);
//        }
//    }


    public UserDTO getVerifiedUser(String username, String password) throws ValidationException, DaoException {

//        Account userAccount = super.getById(Account.class, username); //Throws DaoException if user not found
        Account userAccount;
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT u FROM Account u WHERE u.username=:username";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("username", username);
            userAccount = query.getSingleResult();
        }

        if (!userAccount.verifyPassword(password)) {
            logger.error("{} {}", userAccount.getUsername(), userAccount.getPassword());
            throw new ValidationException("Password does not match");
        }
        return new UserDTO(userAccount.getId().toString(), userAccount.getRoles()
                .stream()
                .map(Roles::toString)
                .collect(Collectors.toSet()));

    }

    public Account createUser(String username, String password) {
        Account userAccount = new Account(username, password);
        userAccount.addRole(Roles.USER);
        try {
            userAccount = this.create(userAccount);
            logger.info("User created (username {})", username);
            return userAccount;
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new EntityExistsException("Error creating user", e);
        }
    }

//    public Account addRoleToUser(String username, Roles role) {
//        Account foundUser = super.getById(Account.class, username);
//        foundUser.addRole(role);
//        try {
//            foundUser = super.update(foundUser);
//            logger.info("Role added to user (username {}, role {})", username, role);
//            return foundUser;
//        } catch (Exception e) {
//            logger.error("Error adding role to user", e);
//            throw new DaoException("Error adding role to user", e);
//        }
//    }

//    public Account removeRoleFromUser(String username, Roles role) {
//        Account foundUserAccount = super.getById(Account.class, username);
//        foundUserAccount.removeRole(role);
//        try {
//            foundUserAccount = super.update(foundUserAccount);
//            logger.info("Role removed from user (username {}, role {})", username, role);
//            return foundUserAccount;
//        } catch (Exception e) {
//            logger.error("Error removing role from user", e);
//            throw new DaoException("Error removing role from user", e);
//        }
//    }

}