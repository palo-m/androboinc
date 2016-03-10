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

package sk.boinc.androboinc.util;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class ClientIdTest {
    private ClientId mClientId;

    @Before
    public void setUp() {
        mClientId = new ClientId(8, "testhost1", "192.168.1.99", 12345, "mypassword");
    }

    @Test
    public void getId() {
        long id = mClientId.getId();
        assertThat(id, is(equalTo(8L)));
    }

    @Test
    public void getNickname() {
        String nickname = mClientId.getNickname();
        assertThat(nickname, is(equalTo("testhost1")));
    }

    @Test
    public void getAddress() {
        String address = mClientId.getAddress();
        assertThat(address, is(equalTo("192.168.1.99")));
    }

    @Test
    public void getPort() {
        int port = mClientId.getPort();
        assertThat(port, is(equalTo(12345)));
    }

    @Test
    public void getPassword() {
        String password = mClientId.getPassword();
        assertThat(password, is(equalTo("mypassword")));
    }

    @Test
    public void parcelRoundTrip() {
        // Write data from mClientId to parcel
        Parcel parcel = Parcel.obtain();
        mClientId.writeToParcel(parcel, 0);
        // Prepare parcel for reading
        parcel.setDataPosition(0);
        // create new object from parcel
        ClientId clientId = ClientId.CREATOR.createFromParcel(parcel);
        // Compare that content is the same (while objects are different)
        assertNotSame("Objects should not be the same", clientId, mClientId);
        assertThat(clientId, is(equalTo(mClientId)));
        assertThat(clientId.getId(), is(equalTo(mClientId.getId())));
        assertThat(clientId.getNickname(), is(equalTo(mClientId.getNickname())));
        assertThat(clientId.getAddress(), is(equalTo(mClientId.getAddress())));
        assertThat(clientId.getPort(), is(equalTo(mClientId.getPort())));
        assertThat(clientId.getPassword(), is(equalTo(mClientId.getPassword())));
    }
}
