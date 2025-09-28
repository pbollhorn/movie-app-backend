# sp-2-team-5: Movie Recommendation API

## Endpoints

URL for API'en er: https://movie.jcoder.dk/api

| Method | URL                               | Request Body (JSON)                        | Response (JSON)                         | Roles  |
|--------|-----------------------------------|--------------------------------------------|-----------------------------------------|--------|
| POST   | /auth/register                    | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| POST   | /auth/login                       | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| GET    | /movies                           | (empty)                                    | `[movieOverview,movieOverview,...]`     | USER   |
| GET    | /movies/(id)                      | (empty)                                    | movieDetails                            | ANYONE | 
| PUT    | /movies/(id)                      | `{"rating": Boolean}`                      | (empty)                                 | USER   |
| DELETE | /movies/(id)                      | (empty)                                    | (empty)                                 | USER   |
| GET    | /movies/recommendations           | (empty)                                    | `[movieOverview,movieOverview,...]`     | USER   |
| GET    | /movies/search?text=(String)      | (empty)                                    | `[movieOverview,movieOverview,...]`     | USER   |
| GET    | /movies/search-open?text=(String) | (empty)                                    | `[movieOverview,movieOverview,...]`     | ANYONE |

```
movieOverview =
{
    "id": Number (samme id som på TMDB),
    "title": String,
    "originalTitle": String,
    "releaseDate": [Number,Number,Number] ([YYYY,MM,DD]),
    "score": Number (fra 0.0 til 10.0, eller NULL hvis filmen har mindre end 10 stemmer),
    "posterPath": String,
    "rating": Boolean
}

movieDetails =
{
    "id": Number (samme id som på TMDB),
    "title": String,
    "originalTitle": String,
    "releaseDate": [Number,Number,Number] ([YYYY,MM,DD]),
    "score": Number (fra 0.0 til 10.0, eller NULL hvis filmen har mindre end 10 stemmer),
    "backdropPath": String,
    "overview": String
}

"rating" er:
 - TRUE hvis brugeren kan lide filmen
 - FALSE hvis brugeren ikke kan lide filmen
 - NULL hvis brugeren ikke har udtrykt sin holdning til filmen
```

## Status på implementation

- Jeg har fået implementeret alle ovenstående endpoints.
- Mine endpoints giver fejlkoder i tilfælde af fejl, men ikke altid de korrekte fejlkoder (4xx for Client Error, 5xx
  Server Error)
- Jeg har ikke fået skrevet særligt mange tests
- Min kode trænger til noget cleanup, f.eks. har jeg både en AbstractDao og en GenericDao
