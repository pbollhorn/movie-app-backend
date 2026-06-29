package dat.controllers;

import java.util.List;

import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.config.HibernateConfig;
import dat.dao.GenreDao;
import dat.dto.TmdbGenreDto;

public class GenreController {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final Logger logger = LoggerFactory.getLogger(GenreController.class);

    public static void getAllGenres(Context ctx) {

        List<TmdbGenreDto> genres = genreDao.getAllGenres();
        ctx.json(genres);
    }


}