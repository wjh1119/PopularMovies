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

import android.net.Uri;
import android.test.AndroidTestCase;

import app.com.example.android.popularmovies.data.MovieContract;

/*
    Students: This is NOT a complete test for the WeatherContract --- just for the functions
    that we expect you to write.
 */
public class TestMovieContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_MOVIE_MODE = "popular";


    public void testBuildMovieMode() {
        Uri modeUri = MovieContract.MovieEntry.buildMovieWithModeUri(TEST_MOVIE_MODE);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                modeUri);
        assertEquals("Error: Movie mode not properly appended to the end of the Uri",
                TEST_MOVIE_MODE, modeUri.getLastPathSegment());
        assertEquals("Error: Movie mode Uri doesn't match our expected result",
                modeUri.toString(),
                "content://app.com.example.android.popularmovies/movie/popular");
    }
}
