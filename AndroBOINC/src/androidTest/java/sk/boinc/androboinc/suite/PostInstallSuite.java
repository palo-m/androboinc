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

package sk.boinc.androboinc.suite;

import sk.boinc.androboinc.NewInstallTest;
import sk.boinc.androboinc.UpgradeTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Tests of behavior after first installation
 * and after upgrade of application
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        NewInstallTest.class,
        UpgradeTest.class
})
public class PostInstallSuite {}
