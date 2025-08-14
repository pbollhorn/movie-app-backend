package dat.config;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.controllers.SecurityController;
import dat.dto.ErrorMessage;
import dat.exceptions.ApiException;

public class ApplicationConfig {
    private static ApplicationConfig instance;
    private static Javalin app;
    private static JavalinConfig javalinConfig;
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final SecurityController securityController = new SecurityController();

    private ApplicationConfig() {
    }

    public static ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
        }
        return instance;
    }

    public ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {
            javalinConfig = config;
            config.showJavalinBanner = false;
            config.http.defaultContentType = "application/json";
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.bundledPlugins.enableDevLogging();
        });
        logger.info("Server initiated");
        return instance;
    }

    public ApplicationConfig setRoute(EndpointGroup routes) {
        javalinConfig.router.apiBuilder(routes);
        logger.info("Routes set");
        return instance;
    }

    public ApplicationConfig checkSecurityRoles() {
        app.beforeMatched(securityController::accessHandler); // authenticate and authorize
        return instance;
    }

    public ApplicationConfig setApiExceptionHandling() {
        // Might be overruled by the setErrorHandling method
        app.exception(ApiException.class, (e, ctx) -> {
            logger.error("ApiException: {}", e.getMessage());
            int statusCode = e.getCode();
            ctx.status(statusCode).json(new ErrorMessage(statusCode, e.getMessage()));
        });
        return instance;
    }

    public ApplicationConfig handleException() {
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Exception: {}", e.getMessage());
            ctx.status(500).json(new ErrorMessage(500, e.getMessage()));
        });
        logger.info("ExceptionHandler initiated");
        return instance;
    }

    public void startServer(int port) {
        app.before(ApplicationConfig::corsHeaders);
        app.options("/*", ApplicationConfig::corsHeadersOptions);
        app.start(port);
        logger.info("Server started on port: {}", port);
    }


    public ApplicationConfig stopServer() {
        app.stop();
        logger.info("Server stopped");
        return instance;
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*"); // Relaxed CORS policy
//        ctx.header("Access-Control-Allow-Origin", "https://movieapp.jcoder.dk"); // Strict CORS policy
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*"); // Relaxed CORS policy
//        ctx.header("Access-Control-Allow-Origin", "https://movieapp.jcoder.dk"); // Strict CORS policy
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }

}
