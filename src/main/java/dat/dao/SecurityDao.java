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

    public UserDTO getVerifiedUser(String email, String password) throws ValidationException, DaoException {

        Account account;
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT a FROM Account a WHERE a.email=:email";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("email", email);
            account = query.getSingleResult();
        }

        if (!account.verifyPassword(password)) {
            logger.error("{} {}", account.getEmail(), account.getHashedPassword());  // TODO: Insecure to log this?
            throw new ValidationException("Password does not match");
        }
        return new UserDTO(account.getId().toString(), Set.of(account.getRole().toString()));
    }


    public Account createUser(String email, String password) {
        Account account = new Account(email, password);
        try {
            account = this.create(account);
            logger.info("User created (email {})", email);
            return account;
        } catch (Exception e) {
            logger.error("Error creating account", e);
            throw new EntityExistsException("Error creating account", e);
        }
    }

}