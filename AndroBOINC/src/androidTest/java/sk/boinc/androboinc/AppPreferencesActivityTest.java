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
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Html;
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
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
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
@LargeTest
public class AppPreferencesActivityTest {
    private static final String TAG = "AppPrefActTest";

    @Rule
    public ActivityTestRule<AppPreferencesActivity> mActivityRule =
            new ActivityTestRule<AppPreferencesActivity>(AppPreferencesActivity.class);

    private String mAboutText;
    private String mLicenseLGPL;
    private String mLicenseGPL;
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

    @Before
    public void setUp() {
        Log.d(TAG, "setUp()");
        Context appCtx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mAboutText = appCtx.getString(R.string.aboutText, appCtx.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME, appCtx.getString(R.string.app_name));
        mLicenseLGPL = Html.fromHtml(readRawResource(appCtx, R.raw.license_lgpl)).toString();
        mLicenseGPL = Html.fromHtml(readRawResource(appCtx, R.raw.license_gpl)).toString();
        mChangeLog = readRawResource(appCtx, R.raw.changelog);
    }

    @Test
    public void displayAbout() {
        Log.d(TAG, "displayAbout()");
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(doesNotExist());
        onView(isRoot()).perform(swipeUp());
        onView(withText(R.string.prefNetworkUsageStats)).check(matches(isDisplayed()));
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.prefAboutTitle)).check(matches(isEnabled()));
        onView(withText(R.string.prefAboutTitle)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.prefAboutTitle))).perform(click());
        // Check that we are in About Dialog
        onView(withText(R.string.prefAboutTitle)).check(doesNotExist());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.aboutTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mAboutText)));
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.homepage)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withId(android.R.id.button3)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withChild(withId(R.id.dialogText))).perform(swipeUp());
        onView(withChild(withId(R.id.dialogText))).perform(swipeDown());
        onView(withId(android.R.id.button2)).perform(click());
        // Check that About Dialog is gone now
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void displayLicenses() {
        Log.d(TAG, "displayLicenses()");
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(doesNotExist());
        onView(isRoot()).perform(swipeUp());
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.license)).check(matches(isDisplayed()));
        onView(withText(R.string.license)).check(matches(isEnabled()));
        onView(withText(R.string.license)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.license))).perform(click());
        // Check that we are in Dialog with LGPL license
        onView(withText(R.string.prefAboutTitle)).check(doesNotExist());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.license)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mLicenseLGPL)));
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.sources)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withId(android.R.id.button3)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button3)).check(matches(withText(R.string.license2)));
        onView(withChild(withId(R.id.dialogText))).perform(swipeUp());
        onView(withChild(withId(R.id.dialogText))).perform(swipeDown());
        onView(withId(android.R.id.button3)).perform(click());
        // Check that we are in Dialog with GPL license
        onView(withText(R.string.prefAboutTitle)).check(doesNotExist());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.license2)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mLicenseGPL)));
        onView(withId(android.R.id.button1)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button3)).check(matches(not(isDisplayed())));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.back)));
        onView(withChild(withId(R.id.dialogText))).perform(swipeUp());
        onView(withChild(withId(R.id.dialogText))).perform(swipeDown());
        onView(withId(android.R.id.button2)).perform(click());
        // Check that Dialog with GPL license is gone now
        onView(withText(R.string.license)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(matches(withText(mLicenseLGPL)));
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.sources)));
        onView(withId(android.R.id.button2)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.dismiss)));
        onView(withId(android.R.id.button3)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button3)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(android.R.id.button3)).check(matches(withText(R.string.license2)));
        onView(withId(android.R.id.button2)).perform(click());
        // Check that Dialog with LGPL license is gone now
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void displayChangeLog() {
        Log.d(TAG, "displayChangeLog()");
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withId(R.id.dialogText)).check(doesNotExist());
        onView(isRoot()).perform(swipeUp());
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.changelog)).check(matches(isDisplayed()));
        onView(withText(R.string.changelog)).check(matches(isEnabled()));
        onView(withText(R.string.changelog)).check(matches(withId(android.R.id.title)));
        onView(withChild(withText(R.string.changelog))).perform(click());
        // Check that we are in Dialog with ChangeLog
        onView(withText(R.string.prefAboutTitle)).check(doesNotExist());
        onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.changelog)).check(matches(isDisplayed()));
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
        // Check that Dialog with ChangeLog is gone now
        onView(isRoot()).inRoot(not(isDialog())).check(matches(isDisplayed()));
        onView(withText(R.string.prefAboutTitle)).check(matches(isDisplayed()));
    }
}
