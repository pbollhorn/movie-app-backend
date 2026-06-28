package dat.dao;

import java.util.Set;

import dk.bugelhartmann.UserDTO;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.entities.Account;
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

    public UserDTO getVerifiedUser(String username, String password) throws ValidationException, DaoException {

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
        return new UserDTO(userAccount.getId().toString(), Set.of(userAccount.getRole().toString()));
    }

    public Account createUser(String username, String password) {
        Account userAccount = new Account(username, password);
        try {
            userAccount = this.create(userAccount);
            logger.info("User created (username {})", username);
            return userAccount;
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new EntityExistsException("Error creating user", e);
        }
    }

}