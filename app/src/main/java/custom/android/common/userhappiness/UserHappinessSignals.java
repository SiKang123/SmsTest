package custom.android.common.userhappiness;

import android.content.Context;
import android.content.Intent;

import custom.android.common.speech.LoggingEvents;
import custom.android.common.speech.LoggingEvents.VoiceIme;

public class UserHappinessSignals {
    public static void userAcceptedImeText(Context context) {
        Intent i = new Intent(LoggingEvents.ACTION_LOG_EVENT);
        i.putExtra(LoggingEvents.EXTRA_APP_NAME, VoiceIme.APP_NAME);
        i.putExtra(LoggingEvents.EXTRA_EVENT, 21);
        i.putExtra(LoggingEvents.EXTRA_CALLING_APP_NAME, context.getPackageName());
        i.putExtra(LoggingEvents.EXTRA_TIMESTAMP, System.currentTimeMillis());
        context.sendBroadcast(i);
    }
}
