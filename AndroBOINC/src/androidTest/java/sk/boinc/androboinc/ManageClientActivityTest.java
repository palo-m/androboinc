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

import android.support.test.espresso.matcher.ViewMatchers;
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
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class ManageClientActivityTest {
    private static final String TAG = "ManageClientActTest";

    @Rule
    public ActivityTestRule<ManageClientActivity> mActivityRule =
            new ActivityTestRule<ManageClientActivity>(ManageClientActivity.class);

    @Before
    public void setUp() {
        Log.d(TAG, "setUp()");
    }

    @Test
    public void notConnected() {
        Log.d(TAG, "notConnected()");
        onView(withText(R.string.selectHostTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.selectHostTitle)).check(matches(isEnabled()));
        onView(withText(R.string.selectHostTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.selectHostTitle))).check(matches(isEnabled()));
        onView(withText(R.string.clientActivityRunTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientActivityRunTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientActivityRunTitle))).check(matches(not(isEnabled())));
        onView(withText(R.string.clientActivityGpuTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientActivityGpuTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientActivityGpuTitle))).check(matches(not(isEnabled())));
        onView(withText(R.string.clientActivityNetworkTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientActivityNetworkTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientActivityNetworkTitle))).check(matches(not(isEnabled())));
        onView(withText(R.string.clientRunBenchTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientRunBenchTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientRunBenchTitle))).check(matches(not(isEnabled())));
        onView(withText(R.string.clientDoNetCommTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientDoNetCommTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientDoNetCommTitle))).check(matches(not(isEnabled())));
        onView(withText(R.string.clientShutdownTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.clientShutdownTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.clientShutdownTitle))).check(matches(not(isEnabled())));
    }

    @Test
    public void selectHostNone() {
        Log.d(TAG, "selectHostNone()");
        onView(withText(R.string.selectHostTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.selectHostTitle)).check(matches(isEnabled()));
        onView(withText(R.string.selectHostTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.selectHostTitle))).check(matches(isEnabled()));
        onView(withChild(withText(R.string.selectHostTitle))).perform(click());
        // Check that we are in HostListActivity
        onView(withText(R.string.selectHostTitle)).check(doesNotExist());
        onView(withId(R.id.hostNoneText1)).check(matches(isDisplayed()));
        onView(withId(R.id.hostNoneText1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.hostNoneText1)).check(matches(withText(R.string.noHostDefined)));
        onView(withId(R.id.hostNoneText2)).check(matches(isDisplayed()));
        onView(withId(R.id.hostNoneText2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.hostNoneText2)).check(matches(withText(R.string.selectHostNoneAvailable)));
        onView(withChild(withId(R.id.hostNoneText1))).check(matches(withId(android.R.id.empty)));
        onView(withChild(withId(R.id.hostNoneText1))).check(matches(isDisplayed()));
        onView(withChild(withId(R.id.hostNoneText1))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(isRoot()).perform(pressBack());
        // Check that we are back in ManageClientActivity
        onView(withText(R.string.selectHostTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.selectHostTitle)).check(matches(isEnabled()));
        onView(withText(R.string.selectHostTitle)).check(matches(withId(android.R.id.title)));
    }
}
