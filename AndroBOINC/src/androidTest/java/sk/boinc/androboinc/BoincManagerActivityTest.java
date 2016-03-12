/*
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010-2016, Pavol Michalec
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sk.boinc.androboinc;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class BoincManagerActivityTest {
    private static final String TAG = "BoincManagerActTest";

    @Rule
    public ActivityTestRule<BoincManagerActivity> mActivityRule =
            new ActivityTestRule<BoincManagerActivity>(BoincManagerActivity.class);

    @Before
    public void setUp() {
        Log.d(TAG, "setUp()");
    }

    @Test
    public void selectTabs() {
        Log.d(TAG, "notConnected()");
        onView(withId(android.R.id.tabhost)).check(matches(isDisplayed()));
        onView(withId(android.R.id.tabs)).check(matches(isDisplayed()));
        onView(withText(R.string.projects)).check(matches(isDisplayed()));
        onView(withText(R.string.tasks)).check(matches(isDisplayed()));
        onView(withText(R.string.transfers)).check(matches(isDisplayed()));
        onView(withText(R.string.messages)).check(matches(isDisplayed()));
        onView(withChild(withText(R.string.projects))).perform(click());
        onView(withChild(withText(R.string.transfers))).perform(click());
        onView(withChild(withText(R.string.messages))).perform(click());
        onView(withChild(withText(R.string.tasks))).perform(click());
    }
}
