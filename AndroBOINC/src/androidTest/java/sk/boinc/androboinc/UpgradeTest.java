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

import sk.boinc.androboinc.util.PreferenceName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import android.widget.ScrollView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class UpgradeTest {
    private static final String TAG = "UpgradeTest";

    @Rule
    public ActivityTestRule<BoincManagerActivity> mActivityRule =
            new ActivityTestRule<BoincManagerActivity>(BoincManagerActivity.class);

    private String mChangeLog;

    private static String readRawResource(Context context, int resourceId) {
        InputStream stream = context.getResources().openRawResource(resourceId);
        assertNotNull(stream);
        StringBuilder sb = new StringBuilder();
        String strLine;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
                sb.append("\n");
            }
        }
        catch (IOException e) {
            fail("IOException");
        }
        return sb.toString();
    }

    private static void setPreviousVersion(int version) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences globalPrefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        globalPrefs.edit().putInt(PreferenceName.UPGRADE_INFO_SHOWN_VERSION, version).commit();
        Log.d(TAG, "setPreviousVersion(" + version + ")");
    }

    @Before
    public void setUp() {
        Log.d(TAG, "setUp()");
        Context appCtx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mChangeLog = readRawResource(appCtx, R.raw.changelog);
    }

    @Test
    public void upgradeFromV1() {
        Log.d(TAG, "upgradeFromV1()");
        mActivityRule.getActivity().finish();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Log.d(TAG, "upgradeFromV1() - finished activity");
        setPreviousVersion(1);
        mActivityRule.getActivity().getApplication().onCreate();
        mActivityRule.launchActivity(new Intent());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mChangeLog)));
        onView(withId(R.id.dialogText)).check(matches(withParent(withClassName(equalTo(ScrollView.class.getName())))));
        onView(withId(android.R.id.button1)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button3)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withChild(withId(R.id.dialogText))).perform(swipeUp());
        onView(withId(android.R.id.button2)).perform(click());
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
    }

    @Test
    public void upgradeFromPrevious() {
        Log.d(TAG, "upgradeFromPrevious()");
        mActivityRule.getActivity().finish();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Log.d(TAG, "upgradeFromPrevious() - finished activity");
        setPreviousVersion(BuildConfig.VERSION_CODE - 1);
        mActivityRule.getActivity().getApplication().onCreate();
        mActivityRule.launchActivity(new Intent());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mChangeLog)));
        onView(withId(R.id.dialogText)).check(matches(withParent(withClassName(equalTo(ScrollView.class.getName())))));
        onView(withId(android.R.id.button1)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button3)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withChild(withId(R.id.dialogText))).perform(swipeUp());
        onView(withChild(withId(R.id.dialogText))).perform(swipeDown());
        onView(withId(android.R.id.button2)).perform(click());
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
    }

    @Test
    public void upgradeNotDone() {
        Log.d(TAG, "upgradeNotDone()");
        mActivityRule.getActivity().finish();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Log.d(TAG, "upgradeNotDone() - finished activity");
        mActivityRule.getActivity().getApplication().onCreate();
        mActivityRule.launchActivity(new Intent());
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
    }
}
