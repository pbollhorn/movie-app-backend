package dat.dao;

import java.util.Set;

import dk.bugelhartmann.UserDTO;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.entities.Account;
import dat.exceptions.DaoException;
import dat.exceptions.ValidationException;

public class AccountDao {

    private static AccountDao instance;
    private static EntityManagerFactory emf;

    private final Logger logger = LoggerFactory.getLogger(AccountDao.class);

    private AccountDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static AccountDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new AccountDao(emf);
        }
        return instance;
    }

    public Account createAccount(String email, String password) {
        try (EntityManager em = emf.createEntityManager()) {
            Account account = new Account(email, password);
            em.getTransaction().begin();
            em.persist(account);
            em.getTransaction().commit();
            logger.info("Created account with email: " + email);
            return account;
        } catch (Exception e) {
            logger.error("Error creating account", e);
            throw new DaoException("Error creating account", e);
        }
    }

    public UserDTO getVerifiedAccount(String email, String password) throws ValidationException, DaoException {

        Account account;
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT a FROM Account a WHERE a.email=:email";
            account = em.createQuery(jpql, Account.class)
                    .setParameter("email", email)
                    .getSingleResult();
        }

        if (account.verifyPassword(password)) {
            return new UserDTO(account.getId().toString(), Set.of(account.getRole().toString()));
        } else {
            throw new ValidationException("Password does not match");
        }

    }

}