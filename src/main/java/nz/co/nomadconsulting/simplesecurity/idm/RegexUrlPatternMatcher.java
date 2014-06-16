/*
 * Copyright 2014 Nomad Consulting Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.nomadconsulting.simplesecurity.idm;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class RegexUrlPatternMatcher implements UrlPatternMatcher {

    private final Set<Pattern> patterns = new HashSet<>();


    public RegexUrlPatternMatcher() {
    }


    public RegexUrlPatternMatcher(final String regex) {
        addPattern(regex);
    }


    @Override
    public boolean matches(final String url) {
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }


    public final void addPattern(final String regex) {
        patterns.add(Pattern.compile(regex));
    }


    @Override
    public String toString() {
        return "RegexUrlPatternMatcher [patterns=" + patterns + "]";
    }
}
