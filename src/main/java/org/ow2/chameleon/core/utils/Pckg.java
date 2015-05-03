/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
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
 * #L%
 */
package org.ow2.chameleon.core.utils;

import org.osgi.framework.Version;

/**
 * Represents a Java package (actually an OSGi exported package). The {@code toString} method returns the export clause.
 */
public class Pckg {

    /**
     * The package's name.
     */
    public final String name;

    /**
     * The package's version.
     */
    public final String version;

    /**
     * Creates a new package.
     *
     * @param name    the package's name, must not be {@code null}
     * @param version the package's version, must not be {@code null}
     */
    public Pckg(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Checks whether two packages are equal. Two packages are equal if the have the same name and version.
     *
     * @param o the object to compare
     * @return {@literal true} if the given object is a {@link org.ow2.chameleon.core.utils.Pckg} and if it as the same
     * name and version as the current {@link org.ow2.chameleon.core.utils.Pckg}. {@literal False} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pckg pckg = (Pckg) o;

        return name.equals(pckg.name) && !(version != null ? !version.equals(pckg.version) : pckg.version != null);

    }

    /**
     * Computes the package's hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    /**
     * Gets the OSGi's version of the package's version.
     *
     * @return the version
     */
    public Version version() {
        return new Version(version);
    }

    /**
     * @return a string representation of the package.
     */
    @Override
    public String toString() {
        return toExportClause();
    }

    /**
     * The export-package clause corresponding of the current package.
     *
     * @return the clause
     */
    public String toExportClause() {
        return name + ";version=" + version;
    }
}
