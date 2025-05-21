# sp-2-team-5: Movie Recommendation API

Jeg har lavet en Movie Recommendation API.
Jeg har brugt koden fra Andrés API Security Guide som udgangspunkt for mit projekt.

Link til GitHub repo: https://github.com/pbollhorn/MovieRecommendationApi

Link til 200x200 pixel
logo: https://github.com/pbollhorn/MovieRecommendationApi/blob/main/src/main/resources/logo_200x200.jpg

Link til deployet API: https://movie.jcoder.dk/api

## Vision for min API

- Min database skal indeholde alle dansksprogede film fra TMDB (5547 pr. 31. marts 2025).
- Brugeren skal kunne fremsøge disse film ud fra tekst i filmenes titel og originaltitel.
- Brugeren skal kunne logge ind, så deres holdninger til film kan blive gemt.
- For hver film skal brugeren kunne angive sin holdning: Kan lide (👍), eller kan ikke lide (👎).
- Brugeren skal kunne se en oversigt over sine holdninger til film, og kunne ændre og slette sine holdninger.
- Brugeren skal kunne få Movie Recommendations, som en algoritme genererer ud fra brugerens holdninger til film.

## Endpoints

URL for API'en er: https://movie.jcoder.dk/api

| Method | URL                               | Request Body (JSON)                        | Response (JSON)                         | Roles  |
|--------|-----------------------------------|--------------------------------------------|-----------------------------------------|--------|
| POST   | /auth/register                    | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| POST   | /auth/login                       | `{"username": String, "password": String}` | `{"token": String, "username": String}` | ANYONE |
| GET    | /movies                           | (empty)                                    | `[movieOverview,movieOverview,...]`     | USER   |
| GET    | /movies/(id)                      | (empty)                                    | movieDetails                            | ANYONE | 
| PUT    | /movies/(id)                      | `{"likes": Boolean}`                       | (empty)                                 | USER   |
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
    "rating": Number (fra 0.0 til 10.0, eller NULL hvis filmen har mindre end 10 stemmer),
    "posterPath": String,
    "likes": Boolean
}

movieDetails =
{
    "id": Number (samme id som på TMDB),
    "title": String,
    "originalTitle": String,
    "releaseDate": [Number,Number,Number] ([YYYY,MM,DD]),
    "rating": Number (fra 0.0 til 10.0, eller NULL hvis filmen har mindre end 10 stemmer),
    "backdropPath": String,
    "overview": String
}

"likes" er:
 - TRUE hvis brugeren kan lide filmen
 - FALSE hvis brugeren ikke kan lide filmen
 - NULL hvis brugeren ikke har udtrykt sin holdning til filmen
```

## Status på implementation

- Jeg har fået implementeret alle ovenstående endpoints.
- Mine endpoints giver fejlkoder i tilfælde af fejl, men ikke altid de korrekte fejlkoder (4xx for Client Error, 5xx
  Server Error)
- Jeg har kun 5112 film i min database, for min kode har et problem med at få hentet alle film ned.
- Jeg har ikke fået skrevet særligt mange tests
- Min kode trænger til noget cleanup, f.eks. har jeg både en AbstractDao og en GenericDao