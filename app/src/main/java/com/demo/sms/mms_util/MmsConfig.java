package com.demo.sms.mms_util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;


import com.demo.sms.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MmsConfig {
    private static final boolean DEBUG = false;
    private static final String DEFAULT_HTTP_KEY_X_WAP_PROFILE = "x-wap-profile";
    private static final String DEFAULT_USER_AGENT = "Android-Mms/2.0";
    private static final boolean LOCAL_LOGV = false;
    private static final int MAX_IMAGE_HEIGHT = 480;
    private static final int MAX_IMAGE_WIDTH = 640;
    private static final String TAG = "MmsConfig";
    private static boolean mAliasEnabled = false;
    private static int mAliasRuleMaxChars = 48;
    private static int mAliasRuleMinChars = 2;
    private static boolean mAllowAttachAudio = true;
    private static int mDefaultMMSMessagesPerThread = 20;
    private static int mDefaultSMSMessagesPerThread = 200;
    private static String mEmailGateway = null;
    private static String mHttpParams = null;
    private static String mHttpParamsLine1Key = null;
    private static int mHttpSocketTimeout = 60000;
    private static int mMaxImageHeight = 480;
    private static int mMaxImageWidth = MAX_IMAGE_WIDTH;
    private static int mMaxMessageCountPerThread = 5000;
    private static int mMaxMessageSize = 307200;
    private static int mMaxSizeScaleForPendingMmsAllowed = 4;
    private static int mMinMessageCountPerThread = 2;
    private static int mMinimumSlideElementDuration = 7;
    private static int mMmsEnabled = 1;
    private static boolean mNotifyWapMMSC = false;
    private static int mRecipientLimit = Integer.MAX_VALUE;
    private static boolean mTransIdEnabled = false;
    private static String mUaProfTagName = DEFAULT_HTTP_KEY_X_WAP_PROFILE;
    private static String mUaProfUrl = null;
    private static String mUserAgent = DEFAULT_USER_AGENT;

    public static void init(Context context) {
        loadMmsSettings(context);
    }

    public static boolean getMmsEnabled() {
        return mMmsEnabled == 1;
    }

    public static int getMaxMessageSize() {
        return mMaxMessageSize;
    }

    public static boolean getTransIdEnabled() {
        return mTransIdEnabled;
    }

    public static String getUserAgent() {
        return mUserAgent;
    }

    public static String getUaProfTagName() {
        return mUaProfTagName;
    }

    public static String getUaProfUrl() {
        return mUaProfUrl;
    }

    public static String getHttpParams() {
        return mHttpParams;
    }

    public static String getHttpParamsLine1Key() {
        return mHttpParamsLine1Key;
    }

    public static String getEmailGateway() {
        return mEmailGateway;
    }

    public static int getMaxImageHeight() {
        return mMaxImageHeight;
    }

    public static int getMaxImageWidth() {
        return mMaxImageWidth;
    }

    public static int getRecipientLimit() {
        return mRecipientLimit;
    }

    public static int getDefaultSMSMessagesPerThread() {
        return mDefaultSMSMessagesPerThread;
    }

    public static int getDefaultMMSMessagesPerThread() {
        return mDefaultMMSMessagesPerThread;
    }

    public static int getMinMessageCountPerThread() {
        return mMinMessageCountPerThread;
    }

    public static int getMaxMessageCountPerThread() {
        return mMaxMessageCountPerThread;
    }

    public static int getHttpSocketTimeout() {
        return mHttpSocketTimeout;
    }

    public static int getMinimumSlideElementDuration() {
        return mMinimumSlideElementDuration;
    }

    public static boolean getNotifyWapMMSC() {
        return mNotifyWapMMSC;
    }

    public static int getMaxSizeScaleForPendingMmsAllowed() {
        return mMaxSizeScaleForPendingMmsAllowed;
    }

    public static boolean isAliasEnabled() {
        return mAliasEnabled;
    }

    public static int getAliasMinChars() {
        return mAliasRuleMinChars;
    }

    public static int getAliasMaxChars() {
        return mAliasRuleMaxChars;
    }

    public static boolean getAllowAttachAudio() {
        return mAllowAttachAudio;
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }

    private static void loadMmsSettings(Context context) {
        String str = "loadMmsSettings caught ";
        String str2 = TAG;
        str = "true";
        XmlResourceParser parser = context.getResources().getXml(R.xml.mms_config);
        try {
            beginDocument(parser, "mms_config");
            loop0:
            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break loop0;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == 4) {
                    text = parser.getText();
                }
                if ("name".equalsIgnoreCase(name)) {
                    if ("bool".equals(tag)) {
                        if ("enabledMMS".equalsIgnoreCase(value)) {
                            int i;
                            if ("true".equalsIgnoreCase(text)) {
                                i = 1;
                            } else {
                                i = 0;
                            }
                            mMmsEnabled = i;
                        } else if ("enabledTransID".equalsIgnoreCase(value)) {
                            mTransIdEnabled = "true".equalsIgnoreCase(text);
                        } else if ("enabledNotifyWapMMSC".equalsIgnoreCase(value)) {
                            mNotifyWapMMSC = "true".equalsIgnoreCase(text);
                        } else if ("aliasEnabled".equalsIgnoreCase(value)) {
                            mAliasEnabled = "true".equalsIgnoreCase(text);
                        } else if ("allowAttachAudio".equalsIgnoreCase(value)) {
                            mAllowAttachAudio = "true".equalsIgnoreCase(text);
                        }
                    } else if ("int".equals(tag)) {
                        if ("maxMessageSize".equalsIgnoreCase(value)) {
                            mMaxMessageSize = Integer.parseInt(text);
                        } else if ("maxImageHeight".equalsIgnoreCase(value)) {
                            mMaxImageHeight = Integer.parseInt(text);
                        } else if ("maxImageWidth".equalsIgnoreCase(value)) {
                            mMaxImageWidth = Integer.parseInt(text);
                        } else if ("defaultSMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultSMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("defaultMMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultMMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("minMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMinMessageCountPerThread = Integer.parseInt(text);
                        } else if ("maxMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMaxMessageCountPerThread = Integer.parseInt(text);
                        } else if ("recipientLimit".equalsIgnoreCase(value)) {
                            mRecipientLimit = Integer.parseInt(text);
                            if (mRecipientLimit < 0) {
                                mRecipientLimit = Integer.MAX_VALUE;
                            }
                        } else if ("httpSocketTimeout".equalsIgnoreCase(value)) {
                            mHttpSocketTimeout = Integer.parseInt(text);
                        } else if ("minimumSlideElementDuration".equalsIgnoreCase(value)) {
                            mMinimumSlideElementDuration = Integer.parseInt(text);
                        } else if ("maxSizeScaleForPendingMmsAllowed".equalsIgnoreCase(value)) {
                            mMaxSizeScaleForPendingMmsAllowed = Integer.parseInt(text);
                        } else if ("aliasMinChars".equalsIgnoreCase(value)) {
                            mAliasRuleMinChars = Integer.parseInt(text);
                        } else if ("aliasMaxChars".equalsIgnoreCase(value)) {
                            mAliasRuleMaxChars = Integer.parseInt(text);
                        }
                    } else if ("string".equals(tag)) {
                        if ("userAgent".equalsIgnoreCase(value)) {
                            mUserAgent = text;
                        } else if ("uaProfTagName".equalsIgnoreCase(value)) {
                            mUaProfTagName = text;
                        } else if ("uaProfUrl".equalsIgnoreCase(value)) {
                            mUaProfUrl = text;
                        } else if ("httpParams".equalsIgnoreCase(value)) {
                            mHttpParams = text;
                        } else if ("httpParamsLine1Key".equalsIgnoreCase(value)) {
                            mHttpParamsLine1Key = text;
                        } else if ("emailGatewayNumber".equalsIgnoreCase(value)) {
                            mEmailGateway = text;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } catch (NumberFormatException e2) {
            Log.e(TAG, "loadMmsSettings caught ", e2);
        } catch (IOException e3) {
            Log.e(TAG, "loadMmsSettings caught ", e3);
        } finally {
            parser.close();
        }
        String errorStr = null;
        if (getMmsEnabled() && mUaProfUrl == null) {
            errorStr = "uaProfUrl";
        }
        if (errorStr != null) {
            String err = String.format("MmsConfig.loadMmsSettings mms_config.xml missing %s setting", new Object[]{errorStr});
            str = TAG;
            Log.e(str2, err);
            throw new RuntimeException(err);
        }
    }
}
