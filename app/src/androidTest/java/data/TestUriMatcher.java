/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import app.com.example.android.popularmovies.data.MovieContract;
import app.com.example.android.popularmovies.data.MovieProvider;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String TEST_MODE = "popular";
    private static final int TEST_RANK = 5;

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_MODE_DIR = MovieContract.MovieEntry.buildMovieWithModeUri(TEST_MODE);
    private static final Uri TEST_MOVIE_WITH_MODE_AND_RANK_DIR = MovieContract.MovieEntry.buildMovieWithModeAndRankUri(TEST_MODE, TEST_RANK);

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The MOVIE WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MODE_DIR), MovieProvider.MOVIE_WITH_MODE);
        assertEquals("Error: The MOVIE WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MODE_AND_RANK_DIR), MovieProvider.MOVIE_WITH_MODE_AND_RANK);
    }
}
