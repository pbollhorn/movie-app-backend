package dat.controllers;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dat.dao.SecurityDao;
import dk.bugelhartmann.UserDTO;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import io.javalin.http.*;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.dto.ErrorMessage;
import dat.entities.Account;
import dat.enums.Roles;
import dat.exceptions.ApiException;
import dat.exceptions.DaoException;
import dat.exceptions.ValidationException;
import dat.utils.PropertyReader;

public class SecurityController {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ITokenSecurity tokenSecurity = new TokenSecurity();
    private static final SecurityDao securityDAO = SecurityDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    // Health check for the API. Used in deployment
    public static void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }


    // to get a token
    public static void login(Context ctx) {
        ObjectNode returnJson = objectMapper.createObjectNode();
        try {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            UserDTO verifiedUser = securityDAO.getVerifiedUser(userInput.getUsername(), userInput.getPassword());
            String token = createToken(verifiedUser);
            returnJson.put("token", token)
                    .put("username", verifiedUser.getUsername());

            ctx.status(HttpStatus.OK).json(returnJson);
        } catch (EntityNotFoundException | ValidationException | DaoException e) {
            logger.error("Error logging in user", e);
            throw new ApiException(401, "Could not verify user", e);
            //ctx.status(HttpStatus.UNAUTHORIZED).json(new ErrorMessage("Could not verify user " + e.getMessage()));
        }
    }

    // to get a user
    public static void register(Context ctx) {
        ObjectNode returnJson = objectMapper.createObjectNode();
        try {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            Account createdUserAccount = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());
            String token = createToken(new UserDTO(createdUserAccount.getId().toString(), Set.of("USER")));
            returnJson.put("token", token)
                    .put("username", createdUserAccount.getUsername());

            ctx.status(HttpStatus.CREATED).json(returnJson);
        } catch (EntityExistsException e) {
            logger.error("Error registering user", e);
            //throw new APIException(422, "Could not register user: User already exists", e);
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT).json(new ErrorMessage("User already exists " + e.getMessage()));
        }
    }


    // to check if a user has access to a route
    public static void accessHandler(Context ctx) {
        // This is a preflight request => no need for authentication
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }

        // 1. Check if endpoint is open to all
        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.isEmpty() || permittedRoles.contains(Roles.ANYONE)) {
            return;
        }

        // Check that token is present and not malformed, and get the User from the token
        UserDTO verifiedTokenUser = getUserFromToken(ctx);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dat.exceptions.APIException(401, "Invalid user or token");
        }
        ctx.attribute("user", verifiedTokenUser);

        if (!userHasAllowedRole(verifiedTokenUser, permittedRoles)) {
            throw new ForbiddenResponse("User does not have the required role to access this endpoint");
            // throw new APIException(403, "User does not have the required role to access this endpoint");
        }

    }

    private static UserDTO getUserFromToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing");
        }
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed");
        }
        return verifyToken(token);
    }


    // to verify a token
    public static void verify(Context ctx) {

        ObjectNode returnJson = objectMapper.createObjectNode();
        UserDTO verifiedTokenUser = getUserFromToken(ctx);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token");
        }
        returnJson.put("msg", "Token is valid");
        ctx.status(HttpStatus.OK).json(returnJson);

    }


    // to check how long a token is valid
    public static void timeToLive(Context ctx) {

        ObjectNode returnJson = objectMapper.createObjectNode();

        UserDTO verifiedTokenUser = getUserFromToken(ctx);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token");
        }

        String token = Objects.requireNonNull(ctx.header("Authorization")).split(" ")[1];
        int timeToLive;
        try {
            timeToLive = tokenSecurity.timeToExpire(token);
        } catch (ParseException e) {
            throw new UnauthorizedResponse("Token could not be parsed. Invalid token");
        }
        logger.info("Time to live: {}", timeToLive);
        LocalDateTime expireTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeToLive), ZoneId.systemDefault());
        ZonedDateTime zTime = expireTime.atZone(ZoneId.systemDefault());
        Long difference = zTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
        returnJson.put("msg", "Token is valid until " + zTime)
                .put("expireTime", zTime.toOffsetDateTime().toString())
                .put("secondsToLive", difference);
        ctx.status(HttpStatus.OK).json(returnJson);

    }


    private static boolean userHasAllowedRole(UserDTO user, Set<RouteRole> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(Roles.valueOf(role.toUpperCase())));
    }

    private static String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = PropertyReader.getPropertyValue("ISSUER");
                TOKEN_EXPIRE_TIME = PropertyReader.getPropertyValue("TOKEN_EXPIRE_TIME");
                SECRET_KEY = PropertyReader.getPropertyValue("SECRET_KEY");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            logger.error("Error creating token", e);
            throw new ApiException(500, "Could not create token");
        }
    }

    private static UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : PropertyReader.getPropertyValue("SECRET_KEY");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new ApiException(403, "Token is not valid");
            }
        } catch (ParseException | ApiException | TokenVerificationException e) {
            logger.error("Error verifying token", e);
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }


    // TODO: Jeg PETER, har tilføjet denne metode
    // This method extracts accountId Integer from token in header
    // If no token in header, then null is returned as accountId
    // (null as accountId represents a non-logged-in user)
    public static Integer getAccountIdFromToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            return null;
        }

        UserDTO userDTO = getUserFromToken(ctx);
        return Integer.parseInt(userDTO.getUsername());
    }

}
