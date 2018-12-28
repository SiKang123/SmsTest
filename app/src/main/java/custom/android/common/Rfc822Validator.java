package custom.android.common;

import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.widget.AutoCompleteTextView.Validator;

import java.util.regex.Pattern;

import custom.android.common.speech.LoggingEvents;

public class Rfc822Validator implements Validator {
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[a-zA-z][a-zA-Z][a-zA-Z]*");
    private String mDomain;

    public Rfc822Validator(String domain) {
        this.mDomain = domain;
    }

    public boolean isValid(CharSequence text) {
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(text);
        return tokens.length == 1 && EMAIL_ADDRESS_PATTERN.matcher(tokens[0].getAddress()).matches();
    }

    private String removeIllegalCharacters(String s) {
        StringBuilder result = new StringBuilder();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (!(c <= ' ' || c > '~' || c == '(' || c == ')' || c == '<' || c == '>' || c == '@' || c == ',' || c == ';' || c == ':' || c == '\\' || c == '\"' || c == '[' || c == ']')) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public CharSequence fixText(CharSequence cs) {
        String str = "@";
        if (TextUtils.getTrimmedLength(cs) == 0) {
            return LoggingEvents.EXTRA_CALLING_APP_NAME;
        }
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(cs);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String text = tokens[i].getAddress();
            int index = text.indexOf(64);
            String str2;
            if (index < 0) {
                str2 = "@";
                tokens[i].setAddress(removeIllegalCharacters(text) + str + this.mDomain);
            } else {
                String fix = removeIllegalCharacters(text.substring(0, index));
                String domain = removeIllegalCharacters(text.substring(index + 1));
                str2 = "@";
                tokens[i].setAddress(fix + str + (domain.length() != 0 ? domain : this.mDomain));
            }
            sb.append(tokens[i].toString());
            if (i + 1 < tokens.length) {
                sb.append(", ");
            }
        }
        return sb;
    }
}
