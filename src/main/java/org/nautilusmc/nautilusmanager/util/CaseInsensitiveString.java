package org.nautilusmc.nautilusmanager.util;

public class CaseInsensitiveString {

    public final String string;

    public CaseInsensitiveString(String s) {
        this.string = s;
    }

    @Override
    public int hashCode() {
        return string.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof String s && s.equalsIgnoreCase(string) || obj instanceof CaseInsensitiveString st && st.string.equalsIgnoreCase(string);
    }

    @Override
    public String toString() {
        return string;
    }
}
