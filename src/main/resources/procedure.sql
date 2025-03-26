-- V1__create_get_movies_by_user_liked_directors_function.sql
CREATE OR REPLACE FUNCTION get_movie_recommendations(account_id INT)
RETURNS TABLE (
    id INT,
    title TEXT,
    director_id INT
) AS $$
BEGIN
RETURN QUERY
SELECT DISTINCT m.id, m.title, m.director_id
FROM movies m
         JOIN user_likes ul ON ul.movie_id = m.id
         JOIN movies director_movies ON director_movies.director_id = m.director_id
WHERE ul.user_id = user_id
  AND director_movies.id != ul.movie_id;
END;
$$ LANGUAGE plpgsql;