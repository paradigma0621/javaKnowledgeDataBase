
    public Movie retrieveMovie(Long movieInfoId){ ///sem CompletableFuture
        //movieInfo
        var movieInfo = invokeMovieInfoService(movieInfoId);
        //review
        var reviews = invokeReviewsService(movieInfoId);
        return new Movie(movieInfo, reviews);
    }

    public CompletableFuture<Movie> retrieveMovie_CF(Long movieInfoId){ // com CompletableFuture
        var movieInfo = CompletableFuture.supplyAsync(()->invokeMovieInfoService(movieInfoId));
        //review
        var reviews =CompletableFuture.supplyAsync(()-> invokeReviewsService(movieInfoId));
        return movieInfo
                .thenCombine(reviews, Movie::new);
    }
