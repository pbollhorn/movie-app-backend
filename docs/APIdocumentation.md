# MovieApp Backend API

## Endpoints

URL for the API: https://movie.jcoder.dk/api

| Method | URL                           | Request Body (JSON)                        | Response (JSON)                         | Roles  |
|--------|-------------------------------|--------------------------------------------|-----------------------------------------|--------|
| POST   | /auth/register                | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| POST   | /auth/login                   | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| GET    | /movies                       | (empty)                                    | `MovieOverviewDto[]`                    | USER   |
| GET    | /movies/(id)                  | (empty)                                    | `MovieDetailsDto`                       | ANYONE | 
| PUT    | /movies/(id)                  | `{"rating": Boolean}`                      | (empty)                                 | USER   |
| DELETE | /movies/(id)                  | (empty)                                    | (empty)                                 | USER   |
| GET    | /movies/recommendations       | (empty)                                    | `MovieOverviewDto[]`                    | USER   |
| GET    | /movies/top100                | (empty)                                    | `MovieOverviewDto[]`                    | ANYONE |
| GET    | /movies/search?title=(String) | (empty)                                    | `MovieOverviewDto[]`                    | ANYONE |
| GET    | /movies/person/(id)           | (empty)                                    | `NameMovieListDto`                      | ANYONE |
| GET    | /movies/collection/(id)       | (empty)                                    | `NameMovieListDto`                      | ANYONE |
| POST   | /movies/update                | (empty)                                    | (empty)                                 | USER   |

```
MovieOverviewDto =
{
    "id": Number (TMDB movie id),
    "title": String,
    "originalLanguage": String (ISO 639-1 code),
    "releaseDate": [Number,Number,Number] ([YYYY,MM,DD]),
    "voteAverage": Number (from 1.0 to 10.0),
    "posterPath": String,
    "directors": String[],
    "genres": String[],
    "rating": Boolean
}

NameMovieListDto =
{
    "name": String,
    "movies": MovieOverviewDto[]
}

MovieDetailsDto =
{
    "id": Number (TMDB movie id),
    "title": String,
    "originalTitle": String,
    "originalLanguage": String (ISO 639-1 code),
    "releaseDate": [Number,Number,Number] ([YYYY,MM,DD]),
    "voteAverage": Number (from 1.0 to 10.0)
    "voteCount": Number,
    "backdropPath": String,
    "overview": String,
    "runtime": Number,
    "genres": String[],
    "collection": {id: Number, name: String} or NULL if movie is not part of a collection,
    "credits": CreditDto[]
}

CreditDto =
{
    "id": String ("department_personId"),
    "personId": Number,
    "name": String,
    "department": String,
    "jobsInDepartment": String[],
    "characters": String[]
}

"rating" is:
 - TRUE if the user likes the movie
 - FALSE if the user does not like the movie
 - NULL if the user has not given his/her opinion of the movie
```

## Status på implementation

- Mine endpoints giver fejlkoder i tilfælde af fejl, men ikke altid de korrekte fejlkoder (4xx for Client Error, 5xx
  Server Error)
- Jeg har ikke fået skrevet særligt mange tests
