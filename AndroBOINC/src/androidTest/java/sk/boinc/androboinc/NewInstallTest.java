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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class NewInstallTest {
    private static final String TAG = "NewInstallTest";

    @Rule
    public ActivityTestRule<BoincManagerActivity> mActivityRule =
            new ActivityTestRule<BoincManagerActivity>(BoincManagerActivity.class);

    private String mFirstRun;

    private static void clearPreferences() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences globalPrefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        globalPrefs.edit().clear().commit();
        Log.d(TAG, "clearPreferences(): done");
    }

    @Before
    public void setUp() {
        Log.d(TAG, "setUp()");
        // 1. Finish the activity that has been created automatically by ActivityTestRule
        mActivityRule.getActivity().finish();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Log.d(TAG, "setUp() - finished activity");
        // 2. Delete all preferences stuff, to simulate fresh install
        clearPreferences();
        // 3. Call Application's onCreate() so cleared preferences are taken into account
        mActivityRule.getActivity().getApplication().onCreate();
        // 4. Finally launch the activity again, now it believes it is fresh install
        mActivityRule.launchActivity(new Intent());
        // 5. Prepare data to check
        Context appCtx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mFirstRun = appCtx.getString(R.string.newInstall, appCtx.getString(R.string.app_name), appCtx.getString(R.string.menuAbout));
    }

    @Test
    public void showInfoDismissButton() {
        Log.d(TAG, "showInfoDismissButton()");
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mFirstRun)));
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.seeWiki)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withId(android.R.id.button3)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(android.R.id.button2)).perform(click());
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
    }

//    // Not reasonable - Launched browser cannot be stopped by this test
//    @Test
//    public void showInfoWikiButton() {
//        Log.d(TAG, "showInfoWikiButton()");
//        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
//        onView(withId(R.id.dialogText)).check(matches(withText(mFirstRun)));
//        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
//        onView(withId(android.R.id.button1)).check(matches(withText(R.string.seeWiki)));
//        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
//        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
//        onView(withId(android.R.id.button1)).perform(click());
//        Log.d(TAG, "showInfoWikiButton() launched browser to show Wiki");
//        SystemClock.sleep(TimeUnit.SECONDS.toMillis(5));
//        Log.d(TAG, "showInfoWikiButton() Resuming");
//    }
}
