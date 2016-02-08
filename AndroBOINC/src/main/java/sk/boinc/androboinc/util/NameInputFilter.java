/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
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

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;


/**
 * This input filter will sanitize names and replace characters 
 * which are known to cause troubles:
 * <ul>
 * <li>apostrophe (U+0027) is replaced by right single quotation (U+2019)</li>
 * </ul>
 *
 */
public class NameInputFilter implements InputFilter {

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		for (int i = start; i < end; i++) {
			if (source.charAt(i) == '\'') {
				// Convert source to String in a robust way
				char[] addedChars = new char[end - start];
				TextUtils.getChars(source, start, end, addedChars, 0);
				String newString = new String(addedChars).replaceAll("\'", "’");
				if (source instanceof Spanned) {
					SpannableString sp = new SpannableString(newString);
					TextUtils.copySpansFrom((Spanned)source, start, end, null, sp, 0);
					return sp;
				} 
				else {
					return newString;
				}
			}
		}
		return null;  // Keep the original source
	}
}
