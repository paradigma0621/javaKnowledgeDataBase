package com.learnjava.apiclient;

import com.learnjava.util.CommonUtil;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoviesClientTest {


    WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/movies")
            .build();

    MoviesClient moviesClient = new MoviesClient(webClient);

    @RepeatedTest(10)
    void retrieveMovie() {
        CommonUtil.startTimer();
        //given
        var movieInfoId = 1L;
        //when
        var movie = moviesClient.retrieveMovie(movieInfoId);

        System.out.println("movie : " + movie);
        CommonUtil.timeTaken();
        //then
        assert movie!=null;
        assertEquals("Batman Begins", movie.getMovieInfo().getName());
        assert movie.getReviewList().size() == 1;
    }


    @RepeatedTest(10)
    void retrieveMovie_CF() {
        CommonUtil.startTimer();
        //given
        var movieInfoId = 1L;
        //when
        var movie = moviesClient.retrieveMovie_CF(movieInfoId)
                .join();

        System.out.println("movie : " + movie);
        CommonUtil.timeTaken();
        //then
        assert movie!=null;
        assertEquals("Batman Begins", movie.getMovieInfo().getName());
        assert movie.getReviewList().size() == 1;
    }
}
