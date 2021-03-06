package org.telegram.messenger;

import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.StaticLayout.Builder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.SparseArray;
import com.baranak.turbogramf.R;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.Emoji.EmojiSpan;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInlineResult;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$DocumentAttribute;
import org.telegram.tgnet.TLRPC$InputStickerSet;
import org.telegram.tgnet.TLRPC$KeyboardButton;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$MessageMedia;
import org.telegram.tgnet.TLRPC$PageBlock;
import org.telegram.tgnet.TLRPC$Peer;
import org.telegram.tgnet.TLRPC$Photo;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$SecureValueType;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEvent;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangePhoto;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeTitle;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDeleteMessage;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantInvite;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantJoin;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantLeave;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleInvites;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSignatures;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionUpdatePinned;
import org.telegram.tgnet.TLRPC$TL_channelAdminRights;
import org.telegram.tgnet.TLRPC$TL_channelBannedRights;
import org.telegram.tgnet.TLRPC$TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC$TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC$TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC$TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC$TL_documentEmpty;
import org.telegram.tgnet.TLRPC$TL_game;
import org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC$TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC$TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC$TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC$TL_message;
import org.telegram.tgnet.TLRPC$TL_messageActionBotAllowed;
import org.telegram.tgnet.TLRPC$TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC$TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC$TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC$TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC$TL_messageActionCreatedBroadcastList;
import org.telegram.tgnet.TLRPC$TL_messageActionCustomAction;
import org.telegram.tgnet.TLRPC$TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC$TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC$TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC$TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC$TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC$TL_messageActionSecureValuesSent;
import org.telegram.tgnet.TLRPC$TL_messageActionTTLChange;
import org.telegram.tgnet.TLRPC$TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC$TL_messageEmpty;
import org.telegram.tgnet.TLRPC$TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC$TL_messageEntityBold;
import org.telegram.tgnet.TLRPC$TL_messageEntityBotCommand;
import org.telegram.tgnet.TLRPC$TL_messageEntityCashtag;
import org.telegram.tgnet.TLRPC$TL_messageEntityCode;
import org.telegram.tgnet.TLRPC$TL_messageEntityEmail;
import org.telegram.tgnet.TLRPC$TL_messageEntityHashtag;
import org.telegram.tgnet.TLRPC$TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC$TL_messageEntityMention;
import org.telegram.tgnet.TLRPC$TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC$TL_messageEntityPhone;
import org.telegram.tgnet.TLRPC$TL_messageEntityPre;
import org.telegram.tgnet.TLRPC$TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC$TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC$TL_messageForwarded_old;
import org.telegram.tgnet.TLRPC$TL_messageForwarded_old2;
import org.telegram.tgnet.TLRPC$TL_messageMediaContact;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument_layer68;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument_layer74;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument_old;
import org.telegram.tgnet.TLRPC$TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC$TL_messageMediaGame;
import org.telegram.tgnet.TLRPC$TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC$TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_layer68;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_layer74;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_old;
import org.telegram.tgnet.TLRPC$TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC$TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC$TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC$TL_messageService;
import org.telegram.tgnet.TLRPC$TL_message_old;
import org.telegram.tgnet.TLRPC$TL_message_old2;
import org.telegram.tgnet.TLRPC$TL_message_old3;
import org.telegram.tgnet.TLRPC$TL_message_old4;
import org.telegram.tgnet.TLRPC$TL_message_secret;
import org.telegram.tgnet.TLRPC$TL_pageBlockCollage;
import org.telegram.tgnet.TLRPC$TL_pageBlockPhoto;
import org.telegram.tgnet.TLRPC$TL_pageBlockSlideshow;
import org.telegram.tgnet.TLRPC$TL_pageBlockVideo;
import org.telegram.tgnet.TLRPC$TL_peerChannel;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC$TL_photo;
import org.telegram.tgnet.TLRPC$TL_photoEmpty;
import org.telegram.tgnet.TLRPC$TL_photoSize;
import org.telegram.tgnet.TLRPC$TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC$TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeAddress;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeBankStatement;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeDriverLicense;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeEmail;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeIdentityCard;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeInternalPassport;
import org.telegram.tgnet.TLRPC$TL_secureValueTypePassport;
import org.telegram.tgnet.TLRPC$TL_secureValueTypePassportRegistration;
import org.telegram.tgnet.TLRPC$TL_secureValueTypePersonalDetails;
import org.telegram.tgnet.TLRPC$TL_secureValueTypePhone;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeRentalAgreement;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeTemporaryRegistration;
import org.telegram.tgnet.TLRPC$TL_secureValueTypeUtilityBill;
import org.telegram.tgnet.TLRPC$TL_webPage;
import org.telegram.tgnet.TLRPC$WebDocument;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanBrowser;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanNoUnderlineBold;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;
import turbogram.Utilities.TurboConfig$BG;

public class MessageObject {
    private static final int LINES_PER_BLOCK = 10;
    public static final int MESSAGE_SEND_STATE_EDITING = 3;
    public static final int MESSAGE_SEND_STATE_SENDING = 1;
    public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;
    public static final int MESSAGE_SEND_STATE_SENT = 0;
    public static final int POSITION_FLAG_BOTTOM = 8;
    public static final int POSITION_FLAG_LEFT = 1;
    public static final int POSITION_FLAG_RIGHT = 2;
    public static final int POSITION_FLAG_TOP = 4;
    public static Pattern urlPattern;
    public boolean attachPathExists;
    public int audioPlayerDuration;
    public float audioProgress;
    public int audioProgressSec;
    public StringBuilder botButtonsLayout;
    public float bufferedProgress;
    public boolean cancelEditing;
    public CharSequence caption;
    public int contentType;
    public int currentAccount;
    public TLRPC$TL_channelAdminLogEvent currentEvent;
    public String customReplyName;
    public String dateKey;
    public boolean deleted;
    public CharSequence editingMessage;
    public ArrayList<TLRPC$MessageEntity> editingMessageEntities;
    public long eventId;
    public boolean forceUpdate;
    private int generatedWithMinSize;
    public float gifState;
    public boolean hasRtl;
    public boolean isDateObject;
    private int isRoundVideoCached;
    public int lastLineWidth;
    private boolean layoutCreated;
    public int linesCount;
    public CharSequence linkDescription;
    public boolean localChannel;
    public long localGroupId;
    public String localName;
    public long localSentGroupId;
    public int localType;
    public String localUserName;
    public boolean mediaExists;
    public TLRPC$Message messageOwner;
    public CharSequence messageText;
    public String monthKey;
    public ArrayList<TLRPC$PhotoSize> photoThumbs;
    public ArrayList<TLRPC$PhotoSize> photoThumbs2;
    public String previousAttachPath;
    public String previousCaption;
    public ArrayList<TLRPC$MessageEntity> previousCaptionEntities;
    public TLRPC$MessageMedia previousMedia;
    public MessageObject replyMessageObject;
    public boolean resendAsIs;
    public int textHeight;
    public ArrayList<TextLayoutBlock> textLayoutBlocks;
    public int textWidth;
    public float textXOffset;
    public int type;
    public boolean useCustomPhoto;
    public CharSequence vCardData;
    public VideoEditedInfo videoEditedInfo;
    public boolean viewsReloaded;
    public int wantedBotKeyboardWidth;

    public static class GroupedMessagePosition {
        public float aspectRatio;
        public boolean edge;
        public int flags;
        public boolean last;
        public int leftSpanOffset;
        public byte maxX;
        public byte maxY;
        public byte minX;
        public byte minY;
        public float ph;
        public int pw;
        public float[] siblingHeights;
        public int spanSize;

        public void set(int minX, int maxX, int minY, int maxY, int w, float h, int flags) {
            this.minX = (byte) minX;
            this.maxX = (byte) maxX;
            this.minY = (byte) minY;
            this.maxY = (byte) maxY;
            this.pw = w;
            this.spanSize = w;
            this.ph = h;
            this.flags = (byte) flags;
        }
    }

    public static class GroupedMessages {
        private int firstSpanAdditionalSize = Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        public long groupId;
        public boolean hasSibling;
        private int maxSizeWidth = 800;
        public ArrayList<MessageObject> messages = new ArrayList();
        public ArrayList<GroupedMessagePosition> posArray = new ArrayList();
        public HashMap<MessageObject, GroupedMessagePosition> positions = new HashMap();

        private class MessageGroupedLayoutAttempt {
            public float[] heights;
            public int[] lineCounts;

            public MessageGroupedLayoutAttempt(int i1, int i2, float f1, float f2) {
                this.lineCounts = new int[]{i1, i2};
                this.heights = new float[]{f1, f2};
            }

            public MessageGroupedLayoutAttempt(int i1, int i2, int i3, float f1, float f2, float f3) {
                this.lineCounts = new int[]{i1, i2, i3};
                this.heights = new float[]{f1, f2, f3};
            }

            public MessageGroupedLayoutAttempt(int i1, int i2, int i3, int i4, float f1, float f2, float f3, float f4) {
                this.lineCounts = new int[]{i1, i2, i3, i4};
                this.heights = new float[]{f1, f2, f3, f4};
            }
        }

        private float multiHeight(float[] array, int start, int end) {
            float sum = 0.0f;
            for (int a = start; a < end; a++) {
                sum += array[a];
            }
            return ((float) this.maxSizeWidth) / sum;
        }

        public void calculate() {
            this.posArray.clear();
            this.positions.clear();
            int count = this.messages.size();
            if (count > 1) {
                MessageObject messageObject;
                GroupedMessagePosition pos;
                StringBuilder proportions = new StringBuilder();
                float averageAspectRatio = 1.0f;
                boolean isOut = false;
                byte maxX = (byte) 0;
                boolean forceCalc = false;
                boolean needShare = false;
                this.hasSibling = false;
                int a = 0;
                while (a < count) {
                    messageObject = (MessageObject) this.messages.get(a);
                    if (a == 0) {
                        isOut = messageObject.isOutOwner();
                        needShare = !isOut && (!(messageObject.messageOwner.fwd_from == null || messageObject.messageOwner.fwd_from.saved_from_peer == null) || (messageObject.messageOwner.from_id > 0 && (messageObject.messageOwner.to_id.channel_id != 0 || messageObject.messageOwner.to_id.chat_id != 0 || (messageObject.messageOwner.media instanceof TLRPC$TL_messageMediaGame) || (messageObject.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice))));
                    }
                    TLRPC$PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    GroupedMessagePosition position = new GroupedMessagePosition();
                    position.last = a == count + -1;
                    position.aspectRatio = photoSize == null ? 1.0f : ((float) photoSize.f794w) / ((float) photoSize.f793h);
                    if (position.aspectRatio > 1.2f) {
                        proportions.append("w");
                    } else if (position.aspectRatio < 0.8f) {
                        proportions.append("n");
                    } else {
                        proportions.append("q");
                    }
                    averageAspectRatio += position.aspectRatio;
                    if (position.aspectRatio > 2.0f) {
                        forceCalc = true;
                    }
                    this.positions.put(messageObject, position);
                    this.posArray.add(position);
                    a++;
                }
                if (needShare) {
                    this.maxSizeWidth -= 50;
                    this.firstSpanAdditionalSize += 50;
                }
                int minHeight = AndroidUtilities.dp(120.0f);
                int minWidth = (int) (((float) AndroidUtilities.dp(120.0f)) / (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) / ((float) this.maxSizeWidth)));
                int paddingsWidth = (int) (((float) AndroidUtilities.dp(40.0f)) / (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) / ((float) this.maxSizeWidth)));
                float maxAspectRatio = ((float) this.maxSizeWidth) / 814.0f;
                averageAspectRatio /= (float) count;
                float height;
                int width;
                if (forceCalc || !(count == 2 || count == 3 || count == 4)) {
                    int firstLine;
                    int secondLine;
                    int thirdLine;
                    float[] croppedRatios = new float[this.posArray.size()];
                    for (a = 0; a < count; a++) {
                        if (averageAspectRatio > 1.1f) {
                            croppedRatios[a] = Math.max(1.0f, ((GroupedMessagePosition) this.posArray.get(a)).aspectRatio);
                        } else {
                            croppedRatios[a] = Math.min(1.0f, ((GroupedMessagePosition) this.posArray.get(a)).aspectRatio);
                        }
                        croppedRatios[a] = Math.max(0.66667f, Math.min(1.7f, croppedRatios[a]));
                    }
                    ArrayList<MessageGroupedLayoutAttempt> attempts = new ArrayList();
                    for (firstLine = 1; firstLine < croppedRatios.length; firstLine++) {
                        secondLine = croppedRatios.length - firstLine;
                        if (firstLine <= 3 && secondLine <= 3) {
                            attempts.add(new MessageGroupedLayoutAttempt(firstLine, secondLine, multiHeight(croppedRatios, 0, firstLine), multiHeight(croppedRatios, firstLine, croppedRatios.length)));
                        }
                    }
                    for (firstLine = 1; firstLine < croppedRatios.length - 1; firstLine++) {
                        for (secondLine = 1; secondLine < croppedRatios.length - firstLine; secondLine++) {
                            thirdLine = (croppedRatios.length - firstLine) - secondLine;
                            if (firstLine <= 3) {
                                if (secondLine <= (averageAspectRatio < 0.85f ? 4 : 3) && thirdLine <= 3) {
                                    attempts.add(new MessageGroupedLayoutAttempt(firstLine, secondLine, thirdLine, multiHeight(croppedRatios, 0, firstLine), multiHeight(croppedRatios, firstLine, firstLine + secondLine), multiHeight(croppedRatios, firstLine + secondLine, croppedRatios.length)));
                                }
                            }
                        }
                    }
                    for (firstLine = 1; firstLine < croppedRatios.length - 2; firstLine++) {
                        secondLine = 1;
                        while (secondLine < croppedRatios.length - firstLine) {
                            thirdLine = 1;
                            while (thirdLine < (croppedRatios.length - firstLine) - secondLine) {
                                int fourthLine = ((croppedRatios.length - firstLine) - secondLine) - thirdLine;
                                if (firstLine <= 3 && secondLine <= 3 && thirdLine <= 3 && fourthLine <= 3) {
                                    attempts.add(new MessageGroupedLayoutAttempt(firstLine, secondLine, thirdLine, fourthLine, multiHeight(croppedRatios, 0, firstLine), multiHeight(croppedRatios, firstLine, firstLine + secondLine), multiHeight(croppedRatios, firstLine + secondLine, (firstLine + secondLine) + thirdLine), multiHeight(croppedRatios, (firstLine + secondLine) + thirdLine, croppedRatios.length)));
                                }
                                thirdLine++;
                            }
                            secondLine++;
                        }
                    }
                    MessageGroupedLayoutAttempt optimal = null;
                    float optimalDiff = 0.0f;
                    float maxHeight = (float) ((this.maxSizeWidth / 3) * 4);
                    for (a = 0; a < attempts.size(); a++) {
                        MessageGroupedLayoutAttempt attempt = (MessageGroupedLayoutAttempt) attempts.get(a);
                        height = 0.0f;
                        float minLineHeight = Float.MAX_VALUE;
                        for (int b = 0; b < attempt.heights.length; b++) {
                            height += attempt.heights[b];
                            if (attempt.heights[b] < minLineHeight) {
                                minLineHeight = attempt.heights[b];
                            }
                        }
                        float diff = Math.abs(height - maxHeight);
                        if (attempt.lineCounts.length > 1 && (attempt.lineCounts[0] > attempt.lineCounts[1] || ((attempt.lineCounts.length > 2 && attempt.lineCounts[1] > attempt.lineCounts[2]) || (attempt.lineCounts.length > 3 && attempt.lineCounts[2] > attempt.lineCounts[3])))) {
                            diff *= 1.2f;
                        }
                        if (minLineHeight < ((float) minWidth)) {
                            diff *= 1.5f;
                        }
                        if (optimal == null || diff < optimalDiff) {
                            optimal = attempt;
                            optimalDiff = diff;
                        }
                    }
                    if (optimal != null) {
                        int index = 0;
                        float y = 0.0f;
                        for (int i = 0; i < optimal.lineCounts.length; i++) {
                            int c = optimal.lineCounts[i];
                            float lineHeight = optimal.heights[i];
                            int spanLeft = this.maxSizeWidth;
                            GroupedMessagePosition posToFix = null;
                            maxX = Math.max(maxX, c - 1);
                            for (int k = 0; k < c; k++) {
                                width = (int) (croppedRatios[index] * lineHeight);
                                spanLeft -= width;
                                pos = (GroupedMessagePosition) this.posArray.get(index);
                                int flags = 0;
                                if (i == 0) {
                                    flags = 0 | 4;
                                }
                                if (i == optimal.lineCounts.length - 1) {
                                    flags |= 8;
                                }
                                if (k == 0) {
                                    flags |= 1;
                                    if (isOut) {
                                        posToFix = pos;
                                    }
                                }
                                if (k == c - 1) {
                                    flags |= 2;
                                    if (!isOut) {
                                        posToFix = pos;
                                    }
                                }
                                pos.set(k, k, i, i, width, lineHeight / 814.0f, flags);
                                index++;
                            }
                            posToFix.pw += spanLeft;
                            posToFix.spanSize += spanLeft;
                            y += lineHeight;
                        }
                    } else {
                        return;
                    }
                } else if (count == 2) {
                    position1 = (GroupedMessagePosition) this.posArray.get(0);
                    position2 = (GroupedMessagePosition) this.posArray.get(1);
                    String pString = proportions.toString();
                    if (!pString.equals("ww") || ((double) averageAspectRatio) <= 1.4d * ((double) maxAspectRatio) || ((double) (position1.aspectRatio - position2.aspectRatio)) >= 0.2d) {
                        if (!pString.equals("ww")) {
                            if (!pString.equals("qq")) {
                                int secondWidth = (int) Math.max(0.4f * ((float) this.maxSizeWidth), (float) Math.round((((float) this.maxSizeWidth) / position1.aspectRatio) / ((1.0f / position1.aspectRatio) + (1.0f / position2.aspectRatio))));
                                int firstWidth = this.maxSizeWidth - secondWidth;
                                if (firstWidth < minWidth) {
                                    int diff2 = minWidth - firstWidth;
                                    firstWidth = minWidth;
                                    secondWidth -= diff2;
                                }
                                height = Math.min(814.0f, (float) Math.round(Math.min(((float) firstWidth) / position1.aspectRatio, ((float) secondWidth) / position2.aspectRatio))) / 814.0f;
                                position1.set(0, 0, 0, 0, firstWidth, height, 13);
                                position2.set(1, 1, 0, 0, secondWidth, height, 14);
                                maxX = (byte) 1;
                            }
                        }
                        width = this.maxSizeWidth / 2;
                        height = ((float) Math.round(Math.min(((float) width) / position1.aspectRatio, Math.min(((float) width) / position2.aspectRatio, 814.0f)))) / 814.0f;
                        position1.set(0, 0, 0, 0, width, height, 13);
                        position2.set(1, 1, 0, 0, width, height, 14);
                        maxX = (byte) 1;
                    } else {
                        height = ((float) Math.round(Math.min(((float) this.maxSizeWidth) / position1.aspectRatio, Math.min(((float) this.maxSizeWidth) / position2.aspectRatio, 814.0f / 2.0f)))) / 814.0f;
                        position1.set(0, 0, 0, 0, this.maxSizeWidth, height, 7);
                        position2.set(0, 0, 1, 1, this.maxSizeWidth, height, 11);
                    }
                } else if (count == 3) {
                    position1 = (GroupedMessagePosition) this.posArray.get(0);
                    position2 = (GroupedMessagePosition) this.posArray.get(1);
                    position3 = (GroupedMessagePosition) this.posArray.get(2);
                    float secondHeight;
                    if (proportions.charAt(0) == 'n') {
                        float thirdHeight = Math.min(0.5f * 814.0f, (float) Math.round((position2.aspectRatio * ((float) this.maxSizeWidth)) / (position3.aspectRatio + position2.aspectRatio)));
                        secondHeight = 814.0f - thirdHeight;
                        int rightWidth = (int) Math.max((float) minWidth, Math.min(((float) this.maxSizeWidth) * 0.5f, (float) Math.round(Math.min(position3.aspectRatio * thirdHeight, position2.aspectRatio * secondHeight))));
                        int leftWidth = Math.round(Math.min((position1.aspectRatio * 814.0f) + ((float) paddingsWidth), (float) (this.maxSizeWidth - rightWidth)));
                        position1.set(0, 0, 0, 1, leftWidth, 1.0f, 13);
                        position2.set(1, 1, 0, 0, rightWidth, secondHeight / 814.0f, 6);
                        position3.set(0, 1, 1, 1, rightWidth, thirdHeight / 814.0f, 10);
                        position3.spanSize = this.maxSizeWidth;
                        position1.siblingHeights = new float[]{thirdHeight / 814.0f, secondHeight / 814.0f};
                        if (isOut) {
                            position1.spanSize = this.maxSizeWidth - rightWidth;
                        } else {
                            position2.spanSize = this.maxSizeWidth - leftWidth;
                            position3.leftSpanOffset = leftWidth;
                        }
                        this.hasSibling = true;
                        maxX = (byte) 1;
                    } else {
                        float firstHeight = ((float) Math.round(Math.min(((float) this.maxSizeWidth) / position1.aspectRatio, 0.66f * 814.0f))) / 814.0f;
                        position1.set(0, 1, 0, 0, this.maxSizeWidth, firstHeight, 7);
                        width = this.maxSizeWidth / 2;
                        secondHeight = Math.min(814.0f - firstHeight, (float) Math.round(Math.min(((float) width) / position2.aspectRatio, ((float) width) / position3.aspectRatio))) / 814.0f;
                        position2.set(0, 0, 1, 1, width, secondHeight, 9);
                        position3.set(1, 1, 1, 1, width, secondHeight, 10);
                        maxX = (byte) 1;
                    }
                } else if (count == 4) {
                    position1 = (GroupedMessagePosition) this.posArray.get(0);
                    position2 = (GroupedMessagePosition) this.posArray.get(1);
                    position3 = (GroupedMessagePosition) this.posArray.get(2);
                    GroupedMessagePosition position4 = (GroupedMessagePosition) this.posArray.get(3);
                    float h0;
                    int w0;
                    if (proportions.charAt(0) == 'w') {
                        h0 = ((float) Math.round(Math.min(((float) this.maxSizeWidth) / position1.aspectRatio, 0.66f * 814.0f))) / 814.0f;
                        position1.set(0, 2, 0, 0, this.maxSizeWidth, h0, 7);
                        float h = (float) Math.round(((float) this.maxSizeWidth) / ((position2.aspectRatio + position3.aspectRatio) + position4.aspectRatio));
                        w0 = (int) Math.max((float) minWidth, Math.min(((float) this.maxSizeWidth) * 0.4f, position2.aspectRatio * h));
                        int w2 = (int) Math.max(Math.max((float) minWidth, ((float) this.maxSizeWidth) * 0.33f), position4.aspectRatio * h);
                        int w1 = (this.maxSizeWidth - w0) - w2;
                        h = Math.min(814.0f - h0, h) / 814.0f;
                        position2.set(0, 0, 1, 1, w0, h, 9);
                        position3.set(1, 1, 1, 1, w1, h, 8);
                        position4.set(2, 2, 1, 1, w2, h, 10);
                        maxX = (byte) 2;
                    } else {
                        int w = Math.max(minWidth, Math.round(814.0f / ((1.0f / ((GroupedMessagePosition) this.posArray.get(3)).aspectRatio) + ((1.0f / position3.aspectRatio) + (1.0f / position2.aspectRatio)))));
                        h0 = Math.min(0.33f, Math.max((float) minHeight, ((float) w) / position2.aspectRatio) / 814.0f);
                        float h1 = Math.min(0.33f, Math.max((float) minHeight, ((float) w) / position3.aspectRatio) / 814.0f);
                        float h2 = (1.0f - h0) - h1;
                        w0 = Math.round(Math.min((position1.aspectRatio * 814.0f) + ((float) paddingsWidth), (float) (this.maxSizeWidth - w)));
                        position1.set(0, 0, 0, 2, w0, (h0 + h1) + h2, 13);
                        position2.set(1, 1, 0, 0, w, h0, 6);
                        position3.set(0, 1, 1, 1, w, h1, 2);
                        position3.spanSize = this.maxSizeWidth;
                        position4.set(0, 1, 2, 2, w, h2, 10);
                        position4.spanSize = this.maxSizeWidth;
                        if (isOut) {
                            position1.spanSize = this.maxSizeWidth - w;
                        } else {
                            position2.spanSize = this.maxSizeWidth - w0;
                            position3.leftSpanOffset = w0;
                            position4.leftSpanOffset = w0;
                        }
                        position1.siblingHeights = new float[]{h0, h1, h2};
                        this.hasSibling = true;
                        maxX = (byte) 1;
                    }
                }
                for (a = 0; a < count; a++) {
                    pos = (GroupedMessagePosition) this.posArray.get(a);
                    if (isOut) {
                        if (pos.minX == (byte) 0) {
                            pos.spanSize += this.firstSpanAdditionalSize;
                        }
                        if ((pos.flags & 2) != 0) {
                            pos.edge = true;
                        }
                    } else {
                        if (pos.maxX == maxX || (pos.flags & 2) != 0) {
                            pos.spanSize += this.firstSpanAdditionalSize;
                        }
                        if ((pos.flags & 1) != 0) {
                            pos.edge = true;
                        }
                    }
                    messageObject = (MessageObject) this.messages.get(a);
                    if (!isOut && messageObject.needDrawAvatar()) {
                        if (pos.edge) {
                            if (pos.spanSize != 1000) {
                                pos.spanSize += 108;
                            }
                            pos.pw += 108;
                        } else if ((pos.flags & 2) != 0) {
                            if (pos.spanSize != 1000) {
                                pos.spanSize -= 108;
                            } else if (pos.leftSpanOffset != 0) {
                                pos.leftSpanOffset += 108;
                            }
                        }
                    }
                }
            }
        }
    }

    public static class TextLayoutBlock {
        public int charactersEnd;
        public int charactersOffset;
        public byte directionFlags;
        public int height;
        public int heightByOffset;
        public StaticLayout textLayout;
        public float textYOffset;

        public boolean isRtl() {
            return (this.directionFlags & 1) != 0 && (this.directionFlags & 2) == 0;
        }
    }

    public static class VCardData {
        private String company;
        private ArrayList<String> emails = new ArrayList();
        private ArrayList<String> phones = new ArrayList();

        public static CharSequence parse(String data) {
            boolean finished = false;
            BufferedReader bufferedReader = new BufferedReader(new StringReader(data));
            String pendingLine = null;
            VCardData currentData = null;
            while (true) {
                VCardData currentData2;
                try {
                    String line = bufferedReader.readLine();
                    String originalLine = line;
                    if (line == null) {
                        break;
                    } else if (!originalLine.startsWith("PHOTO")) {
                        int idx;
                        String[] args;
                        String nameEncoding;
                        String nameCharset;
                        String[] args2;
                        byte[] bytes;
                        String decodedName;
                        String email;
                        if (originalLine.indexOf(58) >= 0) {
                            if (originalLine.startsWith("BEGIN:VCARD")) {
                                currentData2 = new VCardData();
                            } else if (originalLine.startsWith("END:VCARD") && currentData != null) {
                                finished = true;
                                currentData2 = currentData;
                            }
                            if (pendingLine != null) {
                                try {
                                    line = pendingLine + line;
                                    pendingLine = null;
                                } catch (Throwable th) {
                                }
                            }
                            if (line.contains("=QUOTED-PRINTABLE") || !line.endsWith("=")) {
                                idx = line.indexOf(":");
                                if (idx < 0) {
                                    args = new String[2];
                                    args[0] = line.substring(0, idx);
                                    args[1] = line.substring(idx + 1, line.length()).trim();
                                } else {
                                    args = new String[]{line.trim()};
                                }
                                if (args.length >= 2) {
                                    currentData = currentData2;
                                } else if (currentData2 != null) {
                                    currentData = currentData2;
                                } else {
                                    if (args[0].startsWith("ORG")) {
                                        nameEncoding = null;
                                        nameCharset = null;
                                        for (String param : args[0].split(";")) {
                                            args2 = param.split("=");
                                            if (args2.length == 2) {
                                                if (args2[0].equals("CHARSET")) {
                                                    nameCharset = args2[1];
                                                } else if (args2[0].equals("ENCODING")) {
                                                    nameEncoding = args2[1];
                                                }
                                            }
                                        }
                                        currentData2.company = args[1];
                                        if (nameEncoding != null && nameEncoding.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                                            bytes = AndroidUtilities.decodeQuotedPrintable(AndroidUtilities.getStringBytes(currentData2.company));
                                            if (!(bytes == null || bytes.length == 0)) {
                                                decodedName = new String(bytes, nameCharset);
                                                if (decodedName != null) {
                                                    currentData2.company = decodedName;
                                                }
                                            }
                                        }
                                        currentData2.company = currentData2.company.replace(';', ' ');
                                    } else if (args[0].startsWith("TEL")) {
                                        if (args[0].startsWith("EMAIL")) {
                                            email = args[1];
                                            if (email.length() > 0) {
                                                currentData2.emails.add(email);
                                            }
                                        }
                                    } else if (args[1].length() > 0) {
                                        currentData2.phones.add(args[1]);
                                    }
                                    currentData = currentData2;
                                }
                            } else {
                                pendingLine = line.substring(0, line.length() - 1);
                                currentData = currentData2;
                            }
                        }
                        currentData2 = currentData;
                        if (pendingLine != null) {
                            line = pendingLine + line;
                            pendingLine = null;
                        }
                        if (line.contains("=QUOTED-PRINTABLE")) {
                        }
                        idx = line.indexOf(":");
                        if (idx < 0) {
                            args = new String[]{line.trim()};
                        } else {
                            args = new String[2];
                            args[0] = line.substring(0, idx);
                            args[1] = line.substring(idx + 1, line.length()).trim();
                        }
                        if (args.length >= 2) {
                            currentData = currentData2;
                        } else if (currentData2 != null) {
                            if (args[0].startsWith("ORG")) {
                                nameEncoding = null;
                                nameCharset = null;
                                while (r23 < r24) {
                                    args2 = param.split("=");
                                    if (args2.length == 2) {
                                        if (args2[0].equals("CHARSET")) {
                                            nameCharset = args2[1];
                                        } else if (args2[0].equals("ENCODING")) {
                                            nameEncoding = args2[1];
                                        }
                                    }
                                }
                                currentData2.company = args[1];
                                bytes = AndroidUtilities.decodeQuotedPrintable(AndroidUtilities.getStringBytes(currentData2.company));
                                decodedName = new String(bytes, nameCharset);
                                if (decodedName != null) {
                                    currentData2.company = decodedName;
                                }
                                currentData2.company = currentData2.company.replace(';', ' ');
                            } else if (args[0].startsWith("TEL")) {
                                if (args[0].startsWith("EMAIL")) {
                                    email = args[1];
                                    if (email.length() > 0) {
                                        currentData2.emails.add(email);
                                    }
                                }
                            } else if (args[1].length() > 0) {
                                currentData2.phones.add(args[1]);
                            }
                            currentData = currentData2;
                        } else {
                            currentData = currentData2;
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                } catch (Throwable th2) {
                    currentData2 = currentData;
                }
            }
            bufferedReader.close();
            if (finished) {
                int a;
                CharSequence result = new StringBuilder();
                for (a = 0; a < currentData.phones.size(); a++) {
                    if (result.length() > 0) {
                        result.append('\n');
                    }
                    String phone = (String) currentData.phones.get(a);
                    if (phone.contains("#") || phone.contains("*")) {
                        result.append(phone);
                    } else {
                        result.append(PhoneFormat.getInstance().format(phone));
                    }
                }
                for (a = 0; a < currentData.emails.size(); a++) {
                    if (result.length() > 0) {
                        result.append('\n');
                    }
                    result.append(PhoneFormat.getInstance().format((String) currentData.emails.get(a)));
                }
                if (TextUtils.isEmpty(currentData.company)) {
                    return result;
                }
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(currentData.company);
                return result;
            }
            return null;
        }
    }

    public MessageObject(int accountNum, TLRPC$Message message, String formattedMessage, String name, String userName, boolean localMessage, boolean isChannel) {
        this.type = 1000;
        this.localType = localMessage ? 2 : 1;
        this.currentAccount = accountNum;
        this.localName = name;
        this.localUserName = userName;
        this.messageText = formattedMessage;
        this.messageOwner = message;
        this.localChannel = isChannel;
    }

    public MessageObject(int accountNum, TLRPC$Message message, AbstractMap<Integer, User> users, boolean generateLayout) {
        this(accountNum, message, (AbstractMap) users, null, generateLayout);
    }

    public MessageObject(int accountNum, TLRPC$Message message, SparseArray<User> users, boolean generateLayout) {
        this(accountNum, message, (SparseArray) users, null, generateLayout);
    }

    public MessageObject(int accountNum, TLRPC$Message message, boolean generateLayout) {
        this(accountNum, message, null, null, null, null, generateLayout, 0);
    }

    public MessageObject(int accountNum, TLRPC$Message message, AbstractMap<Integer, User> users, AbstractMap<Integer, TLRPC$Chat> chats, boolean generateLayout) {
        this(accountNum, message, (AbstractMap) users, (AbstractMap) chats, generateLayout, 0);
    }

    public MessageObject(int accountNum, TLRPC$Message message, SparseArray<User> users, SparseArray<TLRPC$Chat> chats, boolean generateLayout) {
        this(accountNum, message, null, null, users, chats, generateLayout, 0);
    }

    public MessageObject(int accountNum, TLRPC$Message message, AbstractMap<Integer, User> users, AbstractMap<Integer, TLRPC$Chat> chats, boolean generateLayout, long eid) {
        this(accountNum, message, users, chats, null, null, generateLayout, eid);
    }

    public MessageObject(int accountNum, TLRPC$Message message, AbstractMap<Integer, User> users, AbstractMap<Integer, TLRPC$Chat> chats, SparseArray<User> sUsers, SparseArray<TLRPC$Chat> sChats, boolean generateLayout, long eid) {
        int size;
        int a;
        this.type = 1000;
        Theme.createChatResources(null, true);
        this.currentAccount = accountNum;
        this.messageOwner = message;
        this.eventId = eid;
        if (message.replyMessage != null) {
            this.replyMessageObject = new MessageObject(accountNum, message.replyMessage, users, chats, sUsers, sChats, false, eid);
        }
        User fromUser = null;
        if (message.from_id > 0) {
            if (users != null) {
                fromUser = (User) users.get(Integer.valueOf(message.from_id));
            } else if (sUsers != null) {
                fromUser = (User) sUsers.get(message.from_id);
            }
            if (fromUser == null) {
                fromUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(message.from_id));
            }
        }
        String name;
        if (message instanceof TLRPC$TL_messageService) {
            if (message.action != null) {
                if (message.action instanceof TLRPC$TL_messageActionCustomAction) {
                    this.messageText = message.action.message;
                } else if (message.action instanceof TLRPC$TL_messageActionChatCreate) {
                    if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouCreateGroup", R.string.ActionYouCreateGroup);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionCreateGroup", R.string.ActionCreateGroup), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatDeleteUser) {
                    if (message.action.user_id != message.from_id) {
                        whoUser = null;
                        if (users != null) {
                            whoUser = (User) users.get(Integer.valueOf(message.action.user_id));
                        } else if (sUsers != null) {
                            whoUser = (User) sUsers.get(message.action.user_id);
                        }
                        if (whoUser == null) {
                            whoUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(message.action.user_id));
                        }
                        if (isOut()) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionYouKickUser", R.string.ActionYouKickUser), "un2", whoUser);
                        } else if (message.action.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionKickUserYou", R.string.ActionKickUserYou), "un1", fromUser);
                        } else {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionKickUser", R.string.ActionKickUser), "un2", whoUser);
                            this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                        }
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouLeftUser", R.string.ActionYouLeftUser);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionLeftUser", R.string.ActionLeftUser), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatAddUser) {
                    int singleUserId = this.messageOwner.action.user_id;
                    if (singleUserId == 0 && this.messageOwner.action.users.size() == 1) {
                        singleUserId = ((Integer) this.messageOwner.action.users.get(0)).intValue();
                    }
                    if (singleUserId != 0) {
                        whoUser = null;
                        if (users != null) {
                            whoUser = (User) users.get(Integer.valueOf(singleUserId));
                        } else if (sUsers != null) {
                            whoUser = (User) sUsers.get(singleUserId);
                        }
                        if (whoUser == null) {
                            whoUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(singleUserId));
                        }
                        if (singleUserId == message.from_id) {
                            if (message.to_id.channel_id != 0 && !isMegagroup()) {
                                this.messageText = LocaleController.getString("ChannelJoined", R.string.ChannelJoined);
                            } else if (message.to_id.channel_id == 0 || !isMegagroup()) {
                                if (isOut()) {
                                    this.messageText = LocaleController.getString("ActionAddUserSelfYou", R.string.ActionAddUserSelfYou);
                                } else {
                                    this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserSelf", R.string.ActionAddUserSelf), "un1", fromUser);
                                }
                            } else if (singleUserId == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                                this.messageText = LocaleController.getString("ChannelMegaJoined", R.string.ChannelMegaJoined);
                            } else {
                                this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserSelfMega", R.string.ActionAddUserSelfMega), "un1", fromUser);
                            }
                        } else if (isOut()) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", R.string.ActionYouAddUser), "un2", whoUser);
                        } else if (singleUserId != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", R.string.ActionAddUser), "un2", whoUser);
                            this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                        } else if (message.to_id.channel_id == 0) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserYou", R.string.ActionAddUserYou), "un1", fromUser);
                        } else if (isMegagroup()) {
                            this.messageText = replaceWithLink(LocaleController.getString("MegaAddedBy", R.string.MegaAddedBy), "un1", fromUser);
                        } else {
                            this.messageText = replaceWithLink(LocaleController.getString("ChannelAddedBy", R.string.ChannelAddedBy), "un1", fromUser);
                        }
                    } else if (isOut()) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", R.string.ActionYouAddUser), "un2", message.action.users, users, sUsers);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", R.string.ActionAddUser), "un2", message.action.users, users, sUsers);
                        this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatJoinedByLink) {
                    if (isOut()) {
                        this.messageText = LocaleController.getString("ActionInviteYou", R.string.ActionInviteYou);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionInviteUser", R.string.ActionInviteUser), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatEditPhoto) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelChangedPhoto", R.string.ActionChannelChangedPhoto);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouChangedPhoto", R.string.ActionYouChangedPhoto);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedPhoto", R.string.ActionChangedPhoto), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatEditTitle) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelChangedTitle", R.string.ActionChannelChangedTitle).replace("un2", message.action.title);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouChangedTitle", R.string.ActionYouChangedTitle).replace("un2", message.action.title);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedTitle", R.string.ActionChangedTitle).replace("un2", message.action.title), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatDeletePhoto) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelRemovedPhoto", R.string.ActionChannelRemovedPhoto);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouRemovedPhoto", R.string.ActionYouRemovedPhoto);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionRemovedPhoto", R.string.ActionRemovedPhoto), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionTTLChange) {
                    if (message.action.ttl != 0) {
                        if (isOut()) {
                            this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", R.string.MessageLifetimeChangedOutgoing, new Object[]{LocaleController.formatTTLString(message.action.ttl)});
                        } else {
                            this.messageText = LocaleController.formatString("MessageLifetimeChanged", R.string.MessageLifetimeChanged, new Object[]{UserObject.getFirstName(fromUser), LocaleController.formatTTLString(message.action.ttl)});
                        }
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", R.string.MessageLifetimeYouRemoved);
                    } else {
                        this.messageText = LocaleController.formatString("MessageLifetimeRemoved", R.string.MessageLifetimeRemoved, new Object[]{UserObject.getFirstName(fromUser)});
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionLoginUnknownLocation) {
                    String date;
                    long time = ((long) message.date) * 1000;
                    if (LocaleController.getInstance().formatterDay == null || LocaleController.getInstance().formatterYear == null) {
                        date = "" + message.date;
                    } else {
                        date = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, new Object[]{LocaleController.getInstance().formatterYear.format(time), LocaleController.getInstance().formatterDay.format(time)});
                    }
                    User to_user = UserConfig.getInstance(this.currentAccount).getCurrentUser();
                    if (to_user == null) {
                        if (users != null) {
                            to_user = (User) users.get(Integer.valueOf(this.messageOwner.to_id.user_id));
                        } else if (sUsers != null) {
                            to_user = (User) sUsers.get(this.messageOwner.to_id.user_id);
                        }
                        if (to_user == null) {
                            to_user = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(this.messageOwner.to_id.user_id));
                        }
                    }
                    name = to_user != null ? UserObject.getFirstName(to_user) : "";
                    this.messageText = LocaleController.formatString("NotificationUnrecognizedDevice", R.string.NotificationUnrecognizedDevice, new Object[]{name, date, message.action.title, message.action.address});
                } else if (message.action instanceof TLRPC$TL_messageActionUserJoined) {
                    this.messageText = LocaleController.formatString("NotificationContactJoined", R.string.NotificationContactJoined, new Object[]{UserObject.getUserName(fromUser)});
                } else if (message.action instanceof TLRPC$TL_messageActionUserUpdatedPhoto) {
                    this.messageText = LocaleController.formatString("NotificationContactNewPhoto", R.string.NotificationContactNewPhoto, new Object[]{UserObject.getUserName(fromUser)});
                } else if (message.action instanceof TLRPC$TL_messageEncryptedAction) {
                    if (message.action.encryptedAction instanceof TLRPC$TL_decryptedMessageActionScreenshotMessages) {
                        if (isOut()) {
                            this.messageText = LocaleController.formatString("ActionTakeScreenshootYou", R.string.ActionTakeScreenshootYou, new Object[0]);
                        } else {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionTakeScreenshoot", R.string.ActionTakeScreenshoot), "un1", fromUser);
                        }
                    } else if (message.action.encryptedAction instanceof TLRPC$TL_decryptedMessageActionSetMessageTTL) {
                        if (((TLRPC$TL_decryptedMessageActionSetMessageTTL) message.action.encryptedAction).ttl_seconds != 0) {
                            if (isOut()) {
                                this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", R.string.MessageLifetimeChangedOutgoing, new Object[]{LocaleController.formatTTLString(action.ttl_seconds)});
                            } else {
                                this.messageText = LocaleController.formatString("MessageLifetimeChanged", R.string.MessageLifetimeChanged, new Object[]{UserObject.getFirstName(fromUser), LocaleController.formatTTLString(action.ttl_seconds)});
                            }
                        } else if (isOut()) {
                            this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", R.string.MessageLifetimeYouRemoved);
                        } else {
                            this.messageText = LocaleController.formatString("MessageLifetimeRemoved", R.string.MessageLifetimeRemoved, new Object[]{UserObject.getFirstName(fromUser)});
                        }
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionScreenshotTaken) {
                    if (isOut()) {
                        this.messageText = LocaleController.formatString("ActionTakeScreenshootYou", R.string.ActionTakeScreenshootYou, new Object[0]);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionTakeScreenshoot", R.string.ActionTakeScreenshoot), "un1", fromUser);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionCreatedBroadcastList) {
                    this.messageText = LocaleController.formatString("YouCreatedBroadcastList", R.string.YouCreatedBroadcastList, new Object[0]);
                } else if (message.action instanceof TLRPC$TL_messageActionChannelCreate) {
                    if (isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionCreateMega", R.string.ActionCreateMega);
                    } else {
                        this.messageText = LocaleController.getString("ActionCreateChannel", R.string.ActionCreateChannel);
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionChatMigrateTo) {
                    this.messageText = LocaleController.getString("ActionMigrateFromGroup", R.string.ActionMigrateFromGroup);
                } else if (message.action instanceof TLRPC$TL_messageActionChannelMigrateFrom) {
                    this.messageText = LocaleController.getString("ActionMigrateFromGroup", R.string.ActionMigrateFromGroup);
                } else if (message.action instanceof TLRPC$TL_messageActionPinMessage) {
                    TLRPC$Chat chat;
                    if (fromUser != null) {
                        chat = null;
                    } else if (chats != null) {
                        chat = (TLRPC$Chat) chats.get(Integer.valueOf(message.to_id.channel_id));
                    } else if (sChats != null) {
                        chat = (TLRPC$Chat) sChats.get(message.to_id.channel_id);
                    } else {
                        chat = null;
                    }
                    generatePinMessageText(fromUser, chat);
                } else if (message.action instanceof TLRPC$TL_messageActionHistoryClear) {
                    this.messageText = LocaleController.getString("HistoryCleared", R.string.HistoryCleared);
                } else if (message.action instanceof TLRPC$TL_messageActionGameScore) {
                    generateGameMessageText(fromUser);
                } else if (message.action instanceof TLRPC$TL_messageActionPhoneCall) {
                    TLRPC$TL_messageActionPhoneCall call = (TLRPC$TL_messageActionPhoneCall) this.messageOwner.action;
                    boolean isMissed = call.reason instanceof TLRPC$TL_phoneCallDiscardReasonMissed;
                    if (this.messageOwner.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        if (isMissed) {
                            this.messageText = LocaleController.getString("CallMessageOutgoingMissed", R.string.CallMessageOutgoingMissed);
                        } else {
                            this.messageText = LocaleController.getString("CallMessageOutgoing", R.string.CallMessageOutgoing);
                        }
                    } else if (isMissed) {
                        this.messageText = LocaleController.getString("CallMessageIncomingMissed", R.string.CallMessageIncomingMissed);
                    } else if (call.reason instanceof TLRPC$TL_phoneCallDiscardReasonBusy) {
                        this.messageText = LocaleController.getString("CallMessageIncomingDeclined", R.string.CallMessageIncomingDeclined);
                    } else {
                        this.messageText = LocaleController.getString("CallMessageIncoming", R.string.CallMessageIncoming);
                    }
                    if (call.duration > 0) {
                        String duration = LocaleController.formatCallDuration(call.duration);
                        this.messageText = LocaleController.formatString("CallMessageWithDuration", R.string.CallMessageWithDuration, new Object[]{this.messageText, duration});
                        String _messageText = this.messageText.toString();
                        start = _messageText.indexOf(duration);
                        if (start != -1) {
                            r0 = new SpannableString(this.messageText);
                            int end = start + duration.length();
                            if (start > 0 && _messageText.charAt(start - 1) == '(') {
                                start--;
                            }
                            if (end < _messageText.length() && _messageText.charAt(end) == ')') {
                                end++;
                            }
                            r0.setSpan(new TypefaceSpan(Typeface.DEFAULT), start, end, 0);
                            this.messageText = r0;
                        }
                    }
                } else if (message.action instanceof TLRPC$TL_messageActionPaymentSent) {
                    int uid = (int) getDialogId();
                    if (users != null) {
                        fromUser = (User) users.get(Integer.valueOf(uid));
                    } else if (sUsers != null) {
                        fromUser = (User) sUsers.get(uid);
                    }
                    if (fromUser == null) {
                        fromUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(uid));
                    }
                    generatePaymentSentMessageText(null);
                } else if (message.action instanceof TLRPC$TL_messageActionBotAllowed) {
                    String domain = ((TLRPC$TL_messageActionBotAllowed) message.action).domain;
                    String text = LocaleController.getString("ActionBotAllowed", R.string.ActionBotAllowed);
                    start = text.indexOf("%1$s");
                    r0 = new SpannableString(String.format(text, new Object[]{domain}));
                    if (start >= 0) {
                        r0.setSpan(new URLSpanNoUnderlineBold("http://" + domain), start, domain.length() + start, 33);
                    }
                    this.messageText = r0;
                } else if (message.action instanceof TLRPC$TL_messageActionSecureValuesSent) {
                    TLRPC$TL_messageActionSecureValuesSent valuesSent = (TLRPC$TL_messageActionSecureValuesSent) message.action;
                    StringBuilder str = new StringBuilder();
                    size = valuesSent.types.size();
                    for (a = 0; a < size; a++) {
                        TLRPC$SecureValueType type = (TLRPC$SecureValueType) valuesSent.types.get(a);
                        if (str.length() > 0) {
                            str.append(", ");
                        }
                        if (type instanceof TLRPC$TL_secureValueTypePhone) {
                            str.append(LocaleController.getString("ActionBotDocumentPhone", R.string.ActionBotDocumentPhone));
                        } else if (type instanceof TLRPC$TL_secureValueTypeEmail) {
                            str.append(LocaleController.getString("ActionBotDocumentEmail", R.string.ActionBotDocumentEmail));
                        } else if (type instanceof TLRPC$TL_secureValueTypeAddress) {
                            str.append(LocaleController.getString("ActionBotDocumentAddress", R.string.ActionBotDocumentAddress));
                        } else if (type instanceof TLRPC$TL_secureValueTypePersonalDetails) {
                            str.append(LocaleController.getString("ActionBotDocumentIdentity", R.string.ActionBotDocumentIdentity));
                        } else if (type instanceof TLRPC$TL_secureValueTypePassport) {
                            str.append(LocaleController.getString("ActionBotDocumentPassport", R.string.ActionBotDocumentPassport));
                        } else if (type instanceof TLRPC$TL_secureValueTypeDriverLicense) {
                            str.append(LocaleController.getString("ActionBotDocumentDriverLicence", R.string.ActionBotDocumentDriverLicence));
                        } else if (type instanceof TLRPC$TL_secureValueTypeIdentityCard) {
                            str.append(LocaleController.getString("ActionBotDocumentIdentityCard", R.string.ActionBotDocumentIdentityCard));
                        } else if (type instanceof TLRPC$TL_secureValueTypeUtilityBill) {
                            str.append(LocaleController.getString("ActionBotDocumentUtilityBill", R.string.ActionBotDocumentUtilityBill));
                        } else if (type instanceof TLRPC$TL_secureValueTypeBankStatement) {
                            str.append(LocaleController.getString("ActionBotDocumentBankStatement", R.string.ActionBotDocumentBankStatement));
                        } else if (type instanceof TLRPC$TL_secureValueTypeRentalAgreement) {
                            str.append(LocaleController.getString("ActionBotDocumentRentalAgreement", R.string.ActionBotDocumentRentalAgreement));
                        } else if (type instanceof TLRPC$TL_secureValueTypeInternalPassport) {
                            str.append(LocaleController.getString("ActionBotDocumentInternalPassport", R.string.ActionBotDocumentInternalPassport));
                        } else if (type instanceof TLRPC$TL_secureValueTypePassportRegistration) {
                            str.append(LocaleController.getString("ActionBotDocumentPassportRegistration", R.string.ActionBotDocumentPassportRegistration));
                        } else if (type instanceof TLRPC$TL_secureValueTypeTemporaryRegistration) {
                            str.append(LocaleController.getString("ActionBotDocumentTemporaryRegistration", R.string.ActionBotDocumentTemporaryRegistration));
                        }
                    }
                    User user = null;
                    if (message.to_id != null) {
                        if (users != null) {
                            user = (User) users.get(Integer.valueOf(message.to_id.user_id));
                        } else if (sUsers != null) {
                            user = (User) sUsers.get(message.to_id.user_id);
                        }
                        if (user == null) {
                            user = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(message.to_id.user_id));
                        }
                    }
                    this.messageText = LocaleController.formatString("ActionBotDocuments", R.string.ActionBotDocuments, new Object[]{UserObject.getFirstName(user), str.toString()});
                }
            }
        } else if (isMediaEmpty()) {
            this.messageText = message.message;
        } else if (message.media instanceof TLRPC$TL_messageMediaPhoto) {
            this.messageText = LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
        } else if (isVideo() || ((message.media instanceof TLRPC$TL_messageMediaDocument) && (message.media.document instanceof TLRPC$TL_documentEmpty) && message.media.ttl_seconds != 0)) {
            this.messageText = LocaleController.getString("AttachVideo", R.string.AttachVideo);
        } else if (isVoice()) {
            this.messageText = LocaleController.getString("AttachAudio", R.string.AttachAudio);
        } else if (isRoundVideo()) {
            this.messageText = LocaleController.getString("AttachRound", R.string.AttachRound);
        } else if ((message.media instanceof TLRPC$TL_messageMediaGeo) || (message.media instanceof TLRPC$TL_messageMediaVenue)) {
            this.messageText = LocaleController.getString("AttachLocation", R.string.AttachLocation);
        } else if (message.media instanceof TLRPC$TL_messageMediaGeoLive) {
            this.messageText = LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation);
        } else if (message.media instanceof TLRPC$TL_messageMediaContact) {
            this.messageText = LocaleController.getString("AttachContact", R.string.AttachContact);
            if (!TextUtils.isEmpty(message.media.vcard)) {
                this.vCardData = VCardData.parse(message.media.vcard);
            }
        } else if (message.media instanceof TLRPC$TL_messageMediaGame) {
            this.messageText = message.message;
        } else if (message.media instanceof TLRPC$TL_messageMediaInvoice) {
            this.messageText = message.media.description;
        } else if (message.media instanceof TLRPC$TL_messageMediaUnsupported) {
            this.messageText = LocaleController.getString("UnsupportedMedia", R.string.UnsupportedMedia);
        } else if (message.media instanceof TLRPC$TL_messageMediaDocument) {
            if (isSticker()) {
                String sch = getStrickerChar();
                if (sch == null || sch.length() <= 0) {
                    this.messageText = LocaleController.getString("AttachSticker", R.string.AttachSticker);
                } else {
                    this.messageText = String.format("%s %s", new Object[]{sch, LocaleController.getString("AttachSticker", R.string.AttachSticker)});
                }
            } else if (isMusic()) {
                this.messageText = LocaleController.getString("AttachMusic", R.string.AttachMusic);
            } else if (isGif()) {
                this.messageText = LocaleController.getString("AttachGif", R.string.AttachGif);
            } else {
                name = FileLoader.getDocumentFileName(message.media.document);
                if (name == null || name.length() <= 0) {
                    this.messageText = LocaleController.getString("AttachDocument", R.string.AttachDocument);
                } else {
                    this.messageText = name;
                }
            }
        }
        if (this.messageText == null) {
            this.messageText = "";
        }
        setType();
        measureInlineBotButtons();
        Calendar rightNow = new GregorianCalendar();
        rightNow.setTimeInMillis(((long) this.messageOwner.date) * 1000);
        int dateDay = rightNow.get(6);
        int dateYear = rightNow.get(1);
        int dateMonth = rightNow.get(2);
        this.dateKey = String.format("%d_%02d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth), Integer.valueOf(dateDay)});
        this.monthKey = String.format("%d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth)});
        createMessageSendInfo();
        generateCaption();
        if (generateLayout) {
            TextPaint paint;
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                paint = Theme.chat_msgGameTextPaint;
            } else {
                paint = Theme.chat_msgTextPaint;
            }
            int[] emojiOnly = SharedConfig.allowBigEmoji ? new int[1] : null;
            this.messageText = Emoji.replaceEmoji(this.messageText, paint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false, emojiOnly);
            if (emojiOnly != null && emojiOnly[0] >= 1 && emojiOnly[0] <= 3) {
                TextPaint emojiPaint;
                switch (emojiOnly[0]) {
                    case 1:
                        emojiPaint = Theme.chat_msgTextPaintOneEmoji;
                        size = AndroidUtilities.dp(32.0f);
                        break;
                    case 2:
                        emojiPaint = Theme.chat_msgTextPaintTwoEmoji;
                        size = AndroidUtilities.dp(28.0f);
                        break;
                    default:
                        emojiPaint = Theme.chat_msgTextPaintThreeEmoji;
                        size = AndroidUtilities.dp(24.0f);
                        break;
                }
                EmojiSpan[] spans = (EmojiSpan[]) ((Spannable) this.messageText).getSpans(0, this.messageText.length(), EmojiSpan.class);
                if (spans != null && spans.length > 0) {
                    for (EmojiSpan replaceFontMetrics : spans) {
                        replaceFontMetrics.replaceFontMetrics(emojiPaint.getFontMetricsInt(), size);
                    }
                }
            }
            generateLayout(fromUser);
        }
        this.layoutCreated = generateLayout;
        generateThumbs(false);
        checkMediaExistance();
    }

    private void createDateArray(int accountNum, TLRPC$TL_channelAdminLogEvent event, ArrayList<MessageObject> messageObjects, HashMap<String, ArrayList<MessageObject>> messagesByDays) {
        if (((ArrayList) messagesByDays.get(this.dateKey)) == null) {
            messagesByDays.put(this.dateKey, new ArrayList());
            TLRPC$TL_message dateMsg = new TLRPC$TL_message();
            dateMsg.message = LocaleController.formatDateChat((long) event.date);
            dateMsg.id = 0;
            dateMsg.date = event.date;
            MessageObject dateObj = new MessageObject(accountNum, dateMsg, false);
            dateObj.type = 10;
            dateObj.contentType = 1;
            dateObj.isDateObject = true;
            messageObjects.add(dateObj);
        }
    }

    public MessageObject(int accountNum, TLRPC$TL_channelAdminLogEvent event, ArrayList<MessageObject> messageObjects, HashMap<String, ArrayList<MessageObject>> messagesByDays, TLRPC$Chat chat, int[] mid) {
        int a;
        this.type = 1000;
        TLObject fromUser = null;
        if (event.user_id > 0 && null == null) {
            fromUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(event.user_id));
        }
        this.currentEvent = event;
        Calendar rightNow = new GregorianCalendar();
        rightNow.setTimeInMillis(((long) event.date) * 1000);
        int dateDay = rightNow.get(6);
        int dateYear = rightNow.get(1);
        int dateMonth = rightNow.get(2);
        this.dateKey = String.format("%d_%02d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth), Integer.valueOf(dateDay)});
        this.monthKey = String.format("%d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth)});
        TLRPC$Peer to_id = new TLRPC$TL_peerChannel();
        to_id.channel_id = chat.id;
        TLRPC$Message message = null;
        if (event.action instanceof TLRPC$TL_channelAdminLogEventActionChangeTitle) {
            String title = ((TLRPC$TL_channelAdminLogEventActionChangeTitle) event.action).new_value;
            if (chat.megagroup) {
                this.messageText = replaceWithLink(LocaleController.formatString("EventLogEditedGroupTitle", R.string.EventLogEditedGroupTitle, new Object[]{title}), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.formatString("EventLogEditedChannelTitle", R.string.EventLogEditedChannelTitle, new Object[]{title}), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionChangePhoto) {
            this.messageOwner = new TLRPC$TL_messageService();
            if (event.action.new_photo instanceof TLRPC$TL_chatPhotoEmpty) {
                this.messageOwner.action = new TLRPC$TL_messageActionChatDeletePhoto();
                if (chat.megagroup) {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedWGroupPhoto", R.string.EventLogRemovedWGroupPhoto), "un1", fromUser);
                } else {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedChannelPhoto", R.string.EventLogRemovedChannelPhoto), "un1", fromUser);
                }
            } else {
                this.messageOwner.action = new TLRPC$TL_messageActionChatEditPhoto();
                this.messageOwner.action.photo = new TLRPC$TL_photo();
                TLRPC$TL_photoSize photoSize = new TLRPC$TL_photoSize();
                photoSize.location = event.action.new_photo.photo_small;
                photoSize.type = "s";
                photoSize.h = 80;
                photoSize.w = 80;
                this.messageOwner.action.photo.sizes.add(photoSize);
                photoSize = new TLRPC$TL_photoSize();
                photoSize.location = event.action.new_photo.photo_big;
                photoSize.type = "m";
                photoSize.h = 640;
                photoSize.w = 640;
                this.messageOwner.action.photo.sizes.add(photoSize);
                if (chat.megagroup) {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedGroupPhoto", R.string.EventLogEditedGroupPhoto), "un1", fromUser);
                } else {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedChannelPhoto", R.string.EventLogEditedChannelPhoto), "un1", fromUser);
                }
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionParticipantJoin) {
            if (chat.megagroup) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogGroupJoined", R.string.EventLogGroupJoined), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogChannelJoined", R.string.EventLogChannelJoined), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionParticipantLeave) {
            this.messageOwner = new TLRPC$TL_messageService();
            this.messageOwner.action = new TLRPC$TL_messageActionChatDeleteUser();
            this.messageOwner.action.user_id = event.user_id;
            if (chat.megagroup) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogLeftGroup", R.string.EventLogLeftGroup), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogLeftChannel", R.string.EventLogLeftChannel), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionParticipantInvite) {
            this.messageOwner = new TLRPC$TL_messageService();
            this.messageOwner.action = new TLRPC$TL_messageActionChatAddUser();
            TLObject whoUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(event.action.participant.user_id));
            if (event.action.participant.user_id != this.messageOwner.from_id) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogAdded", R.string.EventLogAdded), "un2", whoUser);
                this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
            } else if (chat.megagroup) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogGroupJoined", R.string.EventLogGroupJoined), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogChannelJoined", R.string.EventLogChannelJoined), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin) {
            this.messageOwner = new TLRPC$TL_message();
            whoUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(event.action.prev_participant.user_id));
            str = LocaleController.getString("EventLogPromoted", R.string.EventLogPromoted);
            r5 = new Object[1];
            r5[0] = getUserName(whoUser, this.messageOwner.entities, str.indexOf("%1$s"));
            r0 = new StringBuilder(String.format(str, r5));
            r0.append("\n");
            TLRPC$TL_channelAdminRights o = event.action.prev_participant.admin_rights;
            TLRPC$TL_channelAdminRights n = event.action.new_participant.admin_rights;
            if (o == null) {
                o = new TLRPC$TL_channelAdminRights();
            }
            if (n == null) {
                n = new TLRPC$TL_channelAdminRights();
            }
            if (o.change_info != n.change_info) {
                r0.append('\n').append(n.change_info ? '+' : '-').append(' ');
                r0.append(chat.megagroup ? LocaleController.getString("EventLogPromotedChangeGroupInfo", R.string.EventLogPromotedChangeGroupInfo) : LocaleController.getString("EventLogPromotedChangeChannelInfo", R.string.EventLogPromotedChangeChannelInfo));
            }
            if (!chat.megagroup) {
                if (o.post_messages != n.post_messages) {
                    r0.append('\n').append(n.post_messages ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogPromotedPostMessages", R.string.EventLogPromotedPostMessages));
                }
                if (o.edit_messages != n.edit_messages) {
                    r0.append('\n').append(n.edit_messages ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogPromotedEditMessages", R.string.EventLogPromotedEditMessages));
                }
            }
            if (o.delete_messages != n.delete_messages) {
                r0.append('\n').append(n.delete_messages ? '+' : '-').append(' ');
                r0.append(LocaleController.getString("EventLogPromotedDeleteMessages", R.string.EventLogPromotedDeleteMessages));
            }
            if (o.add_admins != n.add_admins) {
                r0.append('\n').append(n.add_admins ? '+' : '-').append(' ');
                r0.append(LocaleController.getString("EventLogPromotedAddAdmins", R.string.EventLogPromotedAddAdmins));
            }
            if (chat.megagroup && o.ban_users != n.ban_users) {
                r0.append('\n').append(n.ban_users ? '+' : '-').append(' ');
                r0.append(LocaleController.getString("EventLogPromotedBanUsers", R.string.EventLogPromotedBanUsers));
            }
            if (o.invite_users != n.invite_users) {
                r0.append('\n').append(n.invite_users ? '+' : '-').append(' ');
                r0.append(LocaleController.getString("EventLogPromotedAddUsers", R.string.EventLogPromotedAddUsers));
            }
            if (chat.megagroup && o.pin_messages != n.pin_messages) {
                r0.append('\n').append(n.pin_messages ? '+' : '-').append(' ');
                r0.append(LocaleController.getString("EventLogPromotedPinMessages", R.string.EventLogPromotedPinMessages));
            }
            this.messageText = r0.toString();
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionParticipantToggleBan) {
            this.messageOwner = new TLRPC$TL_message();
            whoUser = MessagesController.getInstance(accountNum).getUser(Integer.valueOf(event.action.prev_participant.user_id));
            TLRPC$TL_channelBannedRights o2 = event.action.prev_participant.banned_rights;
            TLRPC$TL_channelBannedRights n2 = event.action.new_participant.banned_rights;
            if (!chat.megagroup || (n2 != null && n2.view_messages && (n2 == null || o2 == null || n2.until_date == o2.until_date))) {
                if (n2 == null || !(o2 == null || n2.view_messages)) {
                    str = LocaleController.getString("EventLogChannelUnrestricted", R.string.EventLogChannelUnrestricted);
                } else {
                    str = LocaleController.getString("EventLogChannelRestricted", R.string.EventLogChannelRestricted);
                }
                r5 = new Object[1];
                r5[0] = getUserName(whoUser, this.messageOwner.entities, str.indexOf("%1$s"));
                this.messageText = String.format(str, r5);
            } else {
                StringBuilder bannedDuration;
                if (n2 == null || AndroidUtilities.isBannedForever(n2.until_date)) {
                    bannedDuration = new StringBuilder(LocaleController.getString("UserRestrictionsUntilForever", R.string.UserRestrictionsUntilForever));
                } else {
                    bannedDuration = new StringBuilder();
                    int duration = n2.until_date - event.date;
                    int days = ((duration / 60) / 60) / 24;
                    duration -= ((days * 60) * 60) * 24;
                    int hours = (duration / 60) / 60;
                    int minutes = (duration - ((hours * 60) * 60)) / 60;
                    int count = 0;
                    for (a = 0; a < 3; a++) {
                        String addStr = null;
                        if (a == 0) {
                            if (days != 0) {
                                addStr = LocaleController.formatPluralString("Days", days);
                                count++;
                            }
                        } else if (a == 1) {
                            if (hours != 0) {
                                addStr = LocaleController.formatPluralString("Hours", hours);
                                count++;
                            }
                        } else if (minutes != 0) {
                            addStr = LocaleController.formatPluralString("Minutes", minutes);
                            count++;
                        }
                        if (addStr != null) {
                            if (bannedDuration.length() > 0) {
                                bannedDuration.append(", ");
                            }
                            bannedDuration.append(addStr);
                        }
                        if (count == 2) {
                            break;
                        }
                    }
                }
                str = LocaleController.getString("EventLogRestrictedUntil", R.string.EventLogRestrictedUntil);
                r5 = new Object[2];
                r5[0] = getUserName(whoUser, this.messageOwner.entities, str.indexOf("%1$s"));
                r5[1] = bannedDuration.toString();
                r0 = new StringBuilder(String.format(str, r5));
                boolean added = false;
                if (o2 == null) {
                    o2 = new TLRPC$TL_channelBannedRights();
                }
                if (n2 == null) {
                    n2 = new TLRPC$TL_channelBannedRights();
                }
                if (o2.view_messages != n2.view_messages) {
                    if (null == null) {
                        r0.append('\n');
                        added = true;
                    }
                    r0.append('\n').append(!n2.view_messages ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogRestrictedReadMessages", R.string.EventLogRestrictedReadMessages));
                }
                if (o2.send_messages != n2.send_messages) {
                    if (!added) {
                        r0.append('\n');
                        added = true;
                    }
                    r0.append('\n').append(!n2.send_messages ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogRestrictedSendMessages", R.string.EventLogRestrictedSendMessages));
                }
                if (!(o2.send_stickers == n2.send_stickers && o2.send_inline == n2.send_inline && o2.send_gifs == n2.send_gifs && o2.send_games == n2.send_games)) {
                    if (!added) {
                        r0.append('\n');
                        added = true;
                    }
                    r0.append('\n').append(!n2.send_stickers ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogRestrictedSendStickers", R.string.EventLogRestrictedSendStickers));
                }
                if (o2.send_media != n2.send_media) {
                    if (!added) {
                        r0.append('\n');
                        added = true;
                    }
                    r0.append('\n').append(!n2.send_media ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogRestrictedSendMedia", R.string.EventLogRestrictedSendMedia));
                }
                if (o2.embed_links != n2.embed_links) {
                    if (!added) {
                        r0.append('\n');
                    }
                    r0.append('\n').append(!n2.embed_links ? '+' : '-').append(' ');
                    r0.append(LocaleController.getString("EventLogRestrictedSendEmbed", R.string.EventLogRestrictedSendEmbed));
                }
                this.messageText = r0.toString();
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionUpdatePinned) {
            if (event.action.message instanceof TLRPC$TL_messageEmpty) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogUnpinnedMessages", R.string.EventLogUnpinnedMessages), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogPinnedMessages", R.string.EventLogPinnedMessages), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionToggleSignatures) {
            if (((TLRPC$TL_channelAdminLogEventActionToggleSignatures) event.action).new_value) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledSignaturesOn", R.string.EventLogToggledSignaturesOn), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledSignaturesOff", R.string.EventLogToggledSignaturesOff), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionToggleInvites) {
            if (((TLRPC$TL_channelAdminLogEventActionToggleInvites) event.action).new_value) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesOn", R.string.EventLogToggledInvitesOn), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesOff", R.string.EventLogToggledInvitesOff), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionDeleteMessage) {
            this.messageText = replaceWithLink(LocaleController.getString("EventLogDeletedMessages", R.string.EventLogDeletedMessages), "un1", fromUser);
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden) {
            if (((TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden) event.action).new_value) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesHistoryOff", R.string.EventLogToggledInvitesHistoryOff), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesHistoryOn", R.string.EventLogToggledInvitesHistoryOn), "un1", fromUser);
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionChangeAbout) {
            CharSequence string;
            if (chat.megagroup) {
                string = LocaleController.getString("EventLogEditedGroupDescription", R.string.EventLogEditedGroupDescription);
            } else {
                string = LocaleController.getString("EventLogEditedChannelDescription", R.string.EventLogEditedChannelDescription);
            }
            this.messageText = replaceWithLink(string, "un1", fromUser);
            message = new TLRPC$TL_message();
            message.out = false;
            message.unread = false;
            message.from_id = event.user_id;
            message.to_id = to_id;
            message.date = event.date;
            message.message = ((TLRPC$TL_channelAdminLogEventActionChangeAbout) event.action).new_value;
            if (TextUtils.isEmpty(((TLRPC$TL_channelAdminLogEventActionChangeAbout) event.action).prev_value)) {
                message.media = new TLRPC$TL_messageMediaEmpty();
            } else {
                message.media = new TLRPC$TL_messageMediaWebPage();
                message.media.webpage = new TLRPC$TL_webPage();
                message.media.webpage.flags = 10;
                message.media.webpage.display_url = "";
                message.media.webpage.url = "";
                message.media.webpage.site_name = LocaleController.getString("EventLogPreviousGroupDescription", R.string.EventLogPreviousGroupDescription);
                message.media.webpage.description = ((TLRPC$TL_channelAdminLogEventActionChangeAbout) event.action).prev_value;
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionChangeUsername) {
            String newLink = ((TLRPC$TL_channelAdminLogEventActionChangeUsername) event.action).new_value;
            if (TextUtils.isEmpty(newLink)) {
                this.messageText = replaceWithLink(chat.megagroup ? LocaleController.getString("EventLogRemovedGroupLink", R.string.EventLogRemovedGroupLink) : LocaleController.getString("EventLogRemovedChannelLink", R.string.EventLogRemovedChannelLink), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(chat.megagroup ? LocaleController.getString("EventLogChangedGroupLink", R.string.EventLogChangedGroupLink) : LocaleController.getString("EventLogChangedChannelLink", R.string.EventLogChangedChannelLink), "un1", fromUser);
            }
            message = new TLRPC$TL_message();
            message.out = false;
            message.unread = false;
            message.from_id = event.user_id;
            message.to_id = to_id;
            message.date = event.date;
            if (TextUtils.isEmpty(newLink)) {
                message.message = "";
            } else {
                message.message = "https://" + MessagesController.getInstance(accountNum).linkPrefix + "/" + newLink;
            }
            TLRPC$TL_messageEntityUrl url = new TLRPC$TL_messageEntityUrl();
            url.offset = 0;
            url.length = message.message.length();
            message.entities.add(url);
            if (TextUtils.isEmpty(((TLRPC$TL_channelAdminLogEventActionChangeUsername) event.action).prev_value)) {
                message.media = new TLRPC$TL_messageMediaEmpty();
            } else {
                message.media = new TLRPC$TL_messageMediaWebPage();
                message.media.webpage = new TLRPC$TL_webPage();
                message.media.webpage.flags = 10;
                message.media.webpage.display_url = "";
                message.media.webpage.url = "";
                message.media.webpage.site_name = LocaleController.getString("EventLogPreviousLink", R.string.EventLogPreviousLink);
                message.media.webpage.description = "https://" + MessagesController.getInstance(accountNum).linkPrefix + "/" + ((TLRPC$TL_channelAdminLogEventActionChangeUsername) event.action).prev_value;
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionEditMessage) {
            message = new TLRPC$TL_message();
            message.out = false;
            message.unread = false;
            message.from_id = event.user_id;
            message.to_id = to_id;
            message.date = event.date;
            TLRPC$Message newMessage = ((TLRPC$TL_channelAdminLogEventActionEditMessage) event.action).new_message;
            TLRPC$Message oldMessage = ((TLRPC$TL_channelAdminLogEventActionEditMessage) event.action).prev_message;
            if (newMessage.media == null || (newMessage.media instanceof TLRPC$TL_messageMediaEmpty) || (newMessage.media instanceof TLRPC$TL_messageMediaWebPage)) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedMessages", R.string.EventLogEditedMessages), "un1", fromUser);
                message.message = newMessage.message;
                message.media = new TLRPC$TL_messageMediaWebPage();
                message.media.webpage = new TLRPC$TL_webPage();
                message.media.webpage.site_name = LocaleController.getString("EventLogOriginalMessages", R.string.EventLogOriginalMessages);
                if (TextUtils.isEmpty(oldMessage.message)) {
                    message.media.webpage.description = LocaleController.getString("EventLogOriginalCaptionEmpty", R.string.EventLogOriginalCaptionEmpty);
                } else {
                    message.media.webpage.description = oldMessage.message;
                }
            } else {
                boolean changedCaption;
                boolean changedMedia;
                if (TextUtils.equals(newMessage.message, oldMessage.message)) {
                    changedCaption = false;
                } else {
                    changedCaption = true;
                }
                if (newMessage.media.getClass() == oldMessage.media.getClass() && ((newMessage.media.photo == null || oldMessage.media.photo == null || newMessage.media.photo.id == oldMessage.media.photo.id) && (newMessage.media.document == null || oldMessage.media.document == null || newMessage.media.document.id == oldMessage.media.document.id))) {
                    changedMedia = false;
                } else {
                    changedMedia = true;
                }
                if (changedMedia && changedCaption) {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedMediaCaption", R.string.EventLogEditedMediaCaption), "un1", fromUser);
                } else if (changedCaption) {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedCaption", R.string.EventLogEditedCaption), "un1", fromUser);
                } else {
                    this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedMedia", R.string.EventLogEditedMedia), "un1", fromUser);
                }
                message.media = newMessage.media;
                if (changedCaption) {
                    message.media.webpage = new TLRPC$TL_webPage();
                    message.media.webpage.site_name = LocaleController.getString("EventLogOriginalCaption", R.string.EventLogOriginalCaption);
                    if (TextUtils.isEmpty(oldMessage.message)) {
                        message.media.webpage.description = LocaleController.getString("EventLogOriginalCaptionEmpty", R.string.EventLogOriginalCaptionEmpty);
                    } else {
                        message.media.webpage.description = oldMessage.message;
                    }
                }
            }
            message.reply_markup = newMessage.reply_markup;
            if (message.media.webpage != null) {
                message.media.webpage.flags = 10;
                message.media.webpage.display_url = "";
                message.media.webpage.url = "";
            }
        } else if (event.action instanceof TLRPC$TL_channelAdminLogEventActionChangeStickerSet) {
            TLRPC$InputStickerSet newStickerset = ((TLRPC$TL_channelAdminLogEventActionChangeStickerSet) event.action).new_stickerset;
            TLRPC$InputStickerSet oldStickerset = ((TLRPC$TL_channelAdminLogEventActionChangeStickerSet) event.action).new_stickerset;
            if (newStickerset == null || (newStickerset instanceof TLRPC$TL_inputStickerSetEmpty)) {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedStickersSet", R.string.EventLogRemovedStickersSet), "un1", fromUser);
            } else {
                this.messageText = replaceWithLink(LocaleController.getString("EventLogChangedStickersSet", R.string.EventLogChangedStickersSet), "un1", fromUser);
            }
        } else {
            this.messageText = "unsupported " + event.action;
        }
        if (this.messageOwner == null) {
            this.messageOwner = new TLRPC$TL_messageService();
        }
        this.messageOwner.message = this.messageText.toString();
        this.messageOwner.from_id = event.user_id;
        this.messageOwner.date = event.date;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        int i = mid[0];
        mid[0] = i + 1;
        tLRPC$Message.id = i;
        this.eventId = event.id;
        this.messageOwner.out = false;
        this.messageOwner.to_id = new TLRPC$TL_peerChannel();
        this.messageOwner.to_id.channel_id = chat.id;
        this.messageOwner.unread = false;
        if (chat.megagroup) {
            tLRPC$Message = this.messageOwner;
            tLRPC$Message.flags |= Integer.MIN_VALUE;
        }
        MediaController mediaController = MediaController.getInstance();
        if (!(event.action.message == null || (event.action.message instanceof TLRPC$TL_messageEmpty))) {
            message = event.action.message;
        }
        if (message != null) {
            message.out = false;
            int i2 = mid[0];
            mid[0] = i2 + 1;
            message.id = i2;
            message.reply_to_msg_id = 0;
            message.flags &= -32769;
            if (chat.megagroup) {
                message.flags |= Integer.MIN_VALUE;
            }
            MessageObject messageObject = new MessageObject(accountNum, message, null, null, true, this.eventId);
            if (messageObject.contentType >= 0) {
                if (mediaController.isPlayingMessage(messageObject)) {
                    MessageObject player = mediaController.getPlayingMessageObject();
                    messageObject.audioProgress = player.audioProgress;
                    messageObject.audioProgressSec = player.audioProgressSec;
                }
                createDateArray(accountNum, event, messageObjects, messagesByDays);
                messageObjects.add(messageObjects.size() - 1, messageObject);
            } else {
                this.contentType = -1;
            }
        }
        if (this.contentType >= 0) {
            TextPaint paint;
            createDateArray(accountNum, event, messageObjects, messagesByDays);
            messageObjects.add(messageObjects.size() - 1, this);
            if (this.messageText == null) {
                this.messageText = "";
            }
            setType();
            measureInlineBotButtons();
            generateCaption();
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                paint = Theme.chat_msgGameTextPaint;
            } else {
                paint = Theme.chat_msgTextPaint;
            }
            int[] emojiOnly = SharedConfig.allowBigEmoji ? new int[1] : null;
            this.messageText = Emoji.replaceEmoji(this.messageText, paint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false, emojiOnly);
            if (emojiOnly != null && emojiOnly[0] >= 1 && emojiOnly[0] <= 3) {
                TextPaint emojiPaint;
                int size;
                switch (emojiOnly[0]) {
                    case 1:
                        emojiPaint = Theme.chat_msgTextPaintOneEmoji;
                        size = AndroidUtilities.dp(32.0f);
                        break;
                    case 2:
                        emojiPaint = Theme.chat_msgTextPaintTwoEmoji;
                        size = AndroidUtilities.dp(28.0f);
                        break;
                    default:
                        emojiPaint = Theme.chat_msgTextPaintThreeEmoji;
                        size = AndroidUtilities.dp(24.0f);
                        break;
                }
                EmojiSpan[] spans = (EmojiSpan[]) ((Spannable) this.messageText).getSpans(0, this.messageText.length(), EmojiSpan.class);
                if (spans != null && spans.length > 0) {
                    for (EmojiSpan replaceFontMetrics : spans) {
                        replaceFontMetrics.replaceFontMetrics(emojiPaint.getFontMetricsInt(), size);
                    }
                }
            }
            if (mediaController.isPlayingMessage(this)) {
                player = mediaController.getPlayingMessageObject();
                this.audioProgress = player.audioProgress;
                this.audioProgressSec = player.audioProgressSec;
            }
            generateLayout(fromUser);
            this.layoutCreated = true;
            generateThumbs(false);
            checkMediaExistance();
        }
    }

    private String getUserName(User user, ArrayList<TLRPC$MessageEntity> entities, int offset) {
        String name;
        if (user == null) {
            name = "";
        } else {
            name = ContactsController.formatName(user.first_name, user.last_name);
        }
        if (offset >= 0) {
            TLRPC$TL_messageEntityMentionName entity = new TLRPC$TL_messageEntityMentionName();
            entity.user_id = user.id;
            entity.offset = offset;
            entity.length = name.length();
            entities.add(entity);
        }
        if (TextUtils.isEmpty(user.username)) {
            return name;
        }
        if (offset >= 0) {
            entity = new TLRPC$TL_messageEntityMentionName();
            entity.user_id = user.id;
            entity.offset = (name.length() + offset) + 2;
            entity.length = user.username.length() + 1;
            entities.add(entity);
        }
        return String.format("%1$s (@%2$s)", new Object[]{name, user.username});
    }

    public void applyNewText() {
        if (!TextUtils.isEmpty(this.messageOwner.message)) {
            TextPaint paint;
            User fromUser = null;
            if (isFromUser()) {
                fromUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
            }
            this.messageText = this.messageOwner.message;
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                paint = Theme.chat_msgGameTextPaint;
            } else {
                paint = Theme.chat_msgTextPaint;
            }
            this.messageText = Emoji.replaceEmoji(this.messageText, paint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            generateLayout(fromUser);
        }
    }

    public void generateGameMessageText(User fromUser) {
        if (fromUser == null && this.messageOwner.from_id > 0) {
            fromUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
        }
        TLRPC$TL_game game = null;
        if (!(this.replyMessageObject == null || this.replyMessageObject.messageOwner.media == null || this.replyMessageObject.messageOwner.media.game == null)) {
            game = this.replyMessageObject.messageOwner.media.game;
        }
        if (game != null) {
            if (fromUser == null || fromUser.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScoredInGame", R.string.ActionUserScoredInGame, new Object[]{LocaleController.formatPluralString("Points", this.messageOwner.action.score)}), "un1", fromUser);
            } else {
                this.messageText = LocaleController.formatString("ActionYouScoredInGame", R.string.ActionYouScoredInGame, new Object[]{LocaleController.formatPluralString("Points", this.messageOwner.action.score)});
            }
            this.messageText = replaceWithLink(this.messageText, "un2", game);
        } else if (fromUser == null || fromUser.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScored", R.string.ActionUserScored, new Object[]{LocaleController.formatPluralString("Points", this.messageOwner.action.score)}), "un1", fromUser);
        } else {
            this.messageText = LocaleController.formatString("ActionYouScored", R.string.ActionYouScored, new Object[]{LocaleController.formatPluralString("Points", this.messageOwner.action.score)});
        }
    }

    public boolean hasValidReplyMessageObject() {
        return (this.replyMessageObject == null || (this.replyMessageObject.messageOwner instanceof TLRPC$TL_messageEmpty) || (this.replyMessageObject.messageOwner.action instanceof TLRPC$TL_messageActionHistoryClear)) ? false : true;
    }

    public void generatePaymentSentMessageText(User fromUser) {
        String name;
        if (fromUser == null) {
            fromUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf((int) getDialogId()));
        }
        if (fromUser != null) {
            name = UserObject.getFirstName(fromUser);
        } else {
            name = "";
        }
        if (this.replyMessageObject == null || !(this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice)) {
            this.messageText = LocaleController.formatString("PaymentSuccessfullyPaidNoItem", R.string.PaymentSuccessfullyPaidNoItem, new Object[]{LocaleController.getInstance().formatCurrencyString(this.messageOwner.action.total_amount, this.messageOwner.action.currency), name});
            return;
        }
        this.messageText = LocaleController.formatString("PaymentSuccessfullyPaid", R.string.PaymentSuccessfullyPaid, new Object[]{LocaleController.getInstance().formatCurrencyString(this.messageOwner.action.total_amount, this.messageOwner.action.currency), name, this.replyMessageObject.messageOwner.media.title});
    }

    public void generatePinMessageText(User fromUser, TLRPC$Chat chat) {
        if (fromUser == null && chat == null) {
            if (this.messageOwner.from_id > 0) {
                fromUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
            }
            if (fromUser == null) {
                TLObject chat2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.to_id.channel_id));
            }
        }
        CharSequence string;
        String str;
        TLObject fromUser2;
        if (this.replyMessageObject == null || (this.replyMessageObject.messageOwner instanceof TLRPC$TL_messageEmpty) || (this.replyMessageObject.messageOwner.action instanceof TLRPC$TL_messageActionHistoryClear)) {
            string = LocaleController.getString("ActionPinnedNoText", R.string.ActionPinnedNoText);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isMusic()) {
            string = LocaleController.getString("ActionPinnedMusic", R.string.ActionPinnedMusic);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isVideo()) {
            string = LocaleController.getString("ActionPinnedVideo", R.string.ActionPinnedVideo);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isGif()) {
            string = LocaleController.getString("ActionPinnedGif", R.string.ActionPinnedGif);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isVoice()) {
            string = LocaleController.getString("ActionPinnedVoice", R.string.ActionPinnedVoice);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isRoundVideo()) {
            string = LocaleController.getString("ActionPinnedRound", R.string.ActionPinnedRound);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.isSticker()) {
            string = LocaleController.getString("ActionPinnedSticker", R.string.ActionPinnedSticker);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
            string = LocaleController.getString("ActionPinnedFile", R.string.ActionPinnedFile);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaGeo) {
            string = LocaleController.getString("ActionPinnedGeo", R.string.ActionPinnedGeo);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaGeoLive) {
            string = LocaleController.getString("ActionPinnedGeoLive", R.string.ActionPinnedGeoLive);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaContact) {
            string = LocaleController.getString("ActionPinnedContact", R.string.ActionPinnedContact);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
            string = LocaleController.getString("ActionPinnedPhoto", R.string.ActionPinnedPhoto);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else if (this.replyMessageObject.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
            string = LocaleController.formatString("ActionPinnedGame", R.string.ActionPinnedGame, new Object[]{"🎮 " + this.replyMessageObject.messageOwner.media.game.title});
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
            this.messageText = Emoji.replaceEmoji(this.messageText, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
        } else if (this.replyMessageObject.messageText == null || this.replyMessageObject.messageText.length() <= 0) {
            string = LocaleController.getString("ActionPinnedNoText", R.string.ActionPinnedNoText);
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        } else {
            CharSequence mess = this.replyMessageObject.messageText;
            if (mess.length() > 20) {
                mess = mess.subSequence(0, 20) + "...";
            }
            mess = Emoji.replaceEmoji(mess, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            string = LocaleController.formatString("ActionPinnedText", R.string.ActionPinnedText, new Object[]{mess});
            str = "un1";
            if (fromUser == null) {
                fromUser2 = chat2;
            }
            this.messageText = replaceWithLink(string, str, fromUser);
        }
    }

    private TLRPC$Photo getPhotoWithId(TLRPC$WebPage webPage, long id) {
        if (webPage == null || webPage.cached_page == null) {
            return null;
        }
        if (webPage.photo != null && webPage.photo.id == id) {
            return webPage.photo;
        }
        for (int a = 0; a < webPage.cached_page.photos.size(); a++) {
            TLRPC$Photo photo = (TLRPC$Photo) webPage.cached_page.photos.get(a);
            if (photo.id == id) {
                return photo;
            }
        }
        return null;
    }

    private TLRPC$Document getDocumentWithId(TLRPC$WebPage webPage, long id) {
        if (webPage == null || webPage.cached_page == null) {
            return null;
        }
        if (webPage.document != null && webPage.document.id == id) {
            return webPage.document;
        }
        for (int a = 0; a < webPage.cached_page.documents.size(); a++) {
            TLRPC$Document document = (TLRPC$Document) webPage.cached_page.documents.get(a);
            if (document.id == id) {
                return document;
            }
        }
        return null;
    }

    private MessageObject getMessageObjectForBlock(TLRPC$WebPage webPage, TLRPC$PageBlock pageBlock) {
        TLRPC$TL_message message = null;
        if (pageBlock instanceof TLRPC$TL_pageBlockPhoto) {
            TLRPC$Photo photo = getPhotoWithId(webPage, pageBlock.photo_id);
            if (photo == webPage.photo) {
                return this;
            }
            message = new TLRPC$TL_message();
            message.media = new TLRPC$TL_messageMediaPhoto();
            message.media.photo = photo;
        } else if (pageBlock instanceof TLRPC$TL_pageBlockVideo) {
            if (getDocumentWithId(webPage, pageBlock.video_id) == webPage.document) {
                return this;
            }
            message = new TLRPC$TL_message();
            message.media = new TLRPC$TL_messageMediaDocument();
            message.media.document = getDocumentWithId(webPage, pageBlock.video_id);
        }
        message.message = "";
        message.id = Utilities.random.nextInt();
        message.date = this.messageOwner.date;
        message.to_id = this.messageOwner.to_id;
        message.out = this.messageOwner.out;
        message.from_id = this.messageOwner.from_id;
        return new MessageObject(this.currentAccount, message, false);
    }

    public ArrayList<MessageObject> getWebPagePhotos(ArrayList<MessageObject> array, ArrayList<TLRPC$PageBlock> blocksToSearch) {
        ArrayList<MessageObject> messageObjects;
        TLRPC$WebPage webPage = this.messageOwner.media.webpage;
        if (array == null) {
            messageObjects = new ArrayList();
        } else {
            messageObjects = array;
        }
        if (webPage.cached_page != null) {
            ArrayList<TLRPC$PageBlock> blocks;
            if (blocksToSearch == null) {
                blocks = webPage.cached_page.blocks;
            } else {
                blocks = blocksToSearch;
            }
            for (int a = 0; a < blocks.size(); a++) {
                TLRPC$PageBlock block = (TLRPC$PageBlock) blocks.get(a);
                int b;
                if (block instanceof TLRPC$TL_pageBlockSlideshow) {
                    TLRPC$TL_pageBlockSlideshow slideshow = (TLRPC$TL_pageBlockSlideshow) block;
                    for (b = 0; b < slideshow.items.size(); b++) {
                        messageObjects.add(getMessageObjectForBlock(webPage, (TLRPC$PageBlock) slideshow.items.get(b)));
                    }
                } else if (block instanceof TLRPC$TL_pageBlockCollage) {
                    TLRPC$TL_pageBlockCollage slideshow2 = (TLRPC$TL_pageBlockCollage) block;
                    for (b = 0; b < slideshow2.items.size(); b++) {
                        messageObjects.add(getMessageObjectForBlock(webPage, (TLRPC$PageBlock) slideshow2.items.get(b)));
                    }
                }
            }
        }
        return messageObjects;
    }

    public void createMessageSendInfo() {
        if (this.messageOwner.message == null) {
            return;
        }
        if ((this.messageOwner.id < 0 || isEditing()) && this.messageOwner.params != null) {
            String param = (String) this.messageOwner.params.get("ve");
            if (param != null && (isVideo() || isNewGif() || isRoundVideo())) {
                this.videoEditedInfo = new VideoEditedInfo();
                if (this.videoEditedInfo.parseString(param)) {
                    this.videoEditedInfo.roundVideo = isRoundVideo();
                } else {
                    this.videoEditedInfo = null;
                }
            }
            if (this.messageOwner.send_state == 3) {
                param = (String) this.messageOwner.params.get("prevMedia");
                if (param != null) {
                    SerializedData serializedData = new SerializedData(Base64.decode(param, 0));
                    this.previousMedia = TLRPC$MessageMedia.TLdeserialize(serializedData, serializedData.readInt32(false), false);
                    this.previousCaption = serializedData.readString(false);
                    this.previousAttachPath = serializedData.readString(false);
                    int count = serializedData.readInt32(false);
                    this.previousCaptionEntities = new ArrayList(count);
                    for (int a = 0; a < count; a++) {
                        this.previousCaptionEntities.add(TLRPC$MessageEntity.TLdeserialize(serializedData, serializedData.readInt32(false), false));
                    }
                    serializedData.cleanup();
                }
            }
        }
    }

    public void measureInlineBotButtons() {
        this.wantedBotKeyboardWidth = 0;
        if (this.messageOwner.reply_markup instanceof TLRPC$TL_replyInlineMarkup) {
            Theme.createChatResources(null, true);
            if (this.botButtonsLayout == null) {
                this.botButtonsLayout = new StringBuilder();
            } else {
                this.botButtonsLayout.setLength(0);
            }
            for (int a = 0; a < this.messageOwner.reply_markup.rows.size(); a++) {
                TLRPC$TL_keyboardButtonRow row = (TLRPC$TL_keyboardButtonRow) this.messageOwner.reply_markup.rows.get(a);
                int maxButtonSize = 0;
                int size = row.buttons.size();
                for (int b = 0; b < size; b++) {
                    CharSequence text;
                    TLRPC$KeyboardButton button = (TLRPC$KeyboardButton) row.buttons.get(b);
                    this.botButtonsLayout.append(a).append(b);
                    if (!(button instanceof TLRPC$TL_keyboardButtonBuy) || (this.messageOwner.media.flags & 4) == 0) {
                        text = Emoji.replaceEmoji(button.text, Theme.chat_msgBotButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false);
                    } else {
                        text = LocaleController.getString("PaymentReceipt", R.string.PaymentReceipt);
                    }
                    StaticLayout staticLayout = new StaticLayout(text, Theme.chat_msgBotButtonPaint, AndroidUtilities.dp(2000.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (staticLayout.getLineCount() > 0) {
                        float width = staticLayout.getLineWidth(0);
                        float left = staticLayout.getLineLeft(0);
                        if (left < width) {
                            width -= left;
                        }
                        maxButtonSize = Math.max(maxButtonSize, ((int) Math.ceil((double) width)) + AndroidUtilities.dp(4.0f));
                    }
                }
                this.wantedBotKeyboardWidth = Math.max(this.wantedBotKeyboardWidth, ((AndroidUtilities.dp(12.0f) + maxButtonSize) * size) + (AndroidUtilities.dp(5.0f) * (size - 1)));
            }
        }
    }

    public boolean isFcmMessage() {
        return this.localType != 0;
    }

    public void setType() {
        int oldType = this.type;
        this.isRoundVideoCached = 0;
        if ((this.messageOwner instanceof TLRPC$TL_message) || (this.messageOwner instanceof TLRPC$TL_messageForwarded_old2)) {
            if (isMediaEmpty()) {
                this.type = 0;
                if (TextUtils.isEmpty(this.messageText) && this.eventId == 0) {
                    this.messageText = "Empty message";
                }
            } else if (this.messageOwner.media.ttl_seconds != 0 && ((this.messageOwner.media.photo instanceof TLRPC$TL_photoEmpty) || (this.messageOwner.media.document instanceof TLRPC$TL_documentEmpty))) {
                this.contentType = 1;
                this.type = 10;
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
                this.type = 1;
            } else if ((this.messageOwner.media instanceof TLRPC$TL_messageMediaGeo) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaVenue) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaGeoLive)) {
                this.type = 4;
            } else if (isRoundVideo()) {
                this.type = 5;
            } else if (isVideo()) {
                this.type = 3;
            } else if (isVoice()) {
                this.type = 2;
            } else if (isMusic()) {
                this.type = 14;
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaContact) {
                this.type = 12;
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaUnsupported) {
                this.type = 0;
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
                if (this.messageOwner.media.document == null || this.messageOwner.media.document.mime_type == null) {
                    this.type = 9;
                } else if (isGifDocument(this.messageOwner.media.document)) {
                    this.type = 8;
                } else if (this.messageOwner.media.document.mime_type.equals("image/webp") && isSticker()) {
                    this.type = 13;
                } else {
                    this.type = 9;
                }
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                this.type = 0;
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice) {
                this.type = 0;
            }
        } else if (this.messageOwner instanceof TLRPC$TL_messageService) {
            if (this.messageOwner.action instanceof TLRPC$TL_messageActionLoginUnknownLocation) {
                this.type = 0;
            } else if ((this.messageOwner.action instanceof TLRPC$TL_messageActionChatEditPhoto) || (this.messageOwner.action instanceof TLRPC$TL_messageActionUserUpdatedPhoto)) {
                this.contentType = 1;
                this.type = 11;
            } else if (this.messageOwner.action instanceof TLRPC$TL_messageEncryptedAction) {
                if ((this.messageOwner.action.encryptedAction instanceof TLRPC$TL_decryptedMessageActionScreenshotMessages) || (this.messageOwner.action.encryptedAction instanceof TLRPC$TL_decryptedMessageActionSetMessageTTL)) {
                    this.contentType = 1;
                    this.type = 10;
                } else {
                    this.contentType = -1;
                    this.type = -1;
                }
            } else if (this.messageOwner.action instanceof TLRPC$TL_messageActionHistoryClear) {
                this.contentType = -1;
                this.type = -1;
            } else if (this.messageOwner.action instanceof TLRPC$TL_messageActionPhoneCall) {
                this.type = 16;
            } else {
                this.contentType = 1;
                this.type = 10;
            }
        }
        if (oldType != 1000 && oldType != this.type) {
            generateThumbs(false);
        }
    }

    public boolean checkLayout() {
        if (this.type != 0 || this.messageOwner.to_id == null || this.messageText == null || this.messageText.length() == 0) {
            return false;
        }
        if (this.layoutCreated) {
            int newMinSize;
            if (AndroidUtilities.isTablet()) {
                newMinSize = AndroidUtilities.getMinTabletSide();
            } else {
                newMinSize = AndroidUtilities.displaySize.x;
            }
            if (Math.abs(this.generatedWithMinSize - newMinSize) > AndroidUtilities.dp(52.0f)) {
                this.layoutCreated = false;
            }
        }
        if (this.layoutCreated) {
            return false;
        }
        TextPaint paint;
        this.layoutCreated = true;
        User fromUser = null;
        if (isFromUser()) {
            fromUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
            paint = Theme.chat_msgGameTextPaint;
        } else {
            paint = Theme.chat_msgTextPaint;
        }
        this.messageText = Emoji.replaceEmoji(this.messageText, paint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
        generateLayout(fromUser);
        return true;
    }

    public String getMimeType() {
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
            return this.messageOwner.media.document.mime_type;
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice) {
            TLRPC$WebDocument photo = ((TLRPC$TL_messageMediaInvoice) this.messageOwner.media).photo;
            if (photo != null) {
                return photo.mime_type;
            }
        } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
            return "image/jpeg";
        } else {
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) {
                if (this.messageOwner.media.webpage.document != null) {
                    return this.messageOwner.media.document.mime_type;
                }
                if (this.messageOwner.media.webpage.photo != null) {
                    return "image/jpeg";
                }
            }
        }
        return "";
    }

    public static boolean isGifDocument(WebFile document) {
        return document != null && (document.mime_type.equals("image/gif") || isNewGifDocument(document));
    }

    public static boolean isGifDocument(TLRPC$Document document) {
        return (document == null || document.thumb == null || document.mime_type == null || (!document.mime_type.equals("image/gif") && !isNewGifDocument(document))) ? false : true;
    }

    public static boolean isRoundVideoDocument(TLRPC$Document document) {
        if (!(document == null || document.mime_type == null || !document.mime_type.equals(MimeTypes.VIDEO_MP4))) {
            int width = 0;
            int height = 0;
            boolean round = false;
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                    width = attribute.f787w;
                    height = attribute.f787w;
                    round = attribute.round_message;
                }
            }
            if (round && width <= 1280 && height <= 1280) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNewGifDocument(WebFile document) {
        if (!(document == null || document.mime_type == null || !document.mime_type.equals(MimeTypes.VIDEO_MP4))) {
            int width = 0;
            int height = 0;
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (!(attribute instanceof TLRPC$TL_documentAttributeAnimated)) {
                    if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                        width = attribute.f787w;
                        height = attribute.f787w;
                    }
                }
            }
            if (width <= 1280 && height <= 1280) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNewGifDocument(TLRPC$Document document) {
        if (!(document == null || document.mime_type == null || !document.mime_type.equals(MimeTypes.VIDEO_MP4))) {
            int width = 0;
            int height = 0;
            boolean animated = false;
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (attribute instanceof TLRPC$TL_documentAttributeAnimated) {
                    animated = true;
                } else if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                    width = attribute.f787w;
                    height = attribute.f787w;
                }
            }
            if (animated && width <= 1280 && height <= 1280) {
                return true;
            }
        }
        return false;
    }

    public void generateThumbs(boolean update) {
        int a;
        TLRPC$PhotoSize photoObject;
        int b;
        TLRPC$PhotoSize size;
        if (this.messageOwner instanceof TLRPC$TL_messageService) {
            if (!(this.messageOwner.action instanceof TLRPC$TL_messageActionChatEditPhoto)) {
                return;
            }
            if (!update) {
                this.photoThumbs = new ArrayList(this.messageOwner.action.photo.sizes);
            } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty()) {
                for (a = 0; a < this.photoThumbs.size(); a++) {
                    photoObject = (TLRPC$PhotoSize) this.photoThumbs.get(a);
                    for (b = 0; b < this.messageOwner.action.photo.sizes.size(); b++) {
                        size = (TLRPC$PhotoSize) this.messageOwner.action.photo.sizes.get(b);
                        if (!(size instanceof TLRPC$TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                            photoObject.location = size.location;
                            break;
                        }
                    }
                }
            }
        } else if (this.messageOwner.media != null && !(this.messageOwner.media instanceof TLRPC$TL_messageMediaEmpty)) {
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
                if (!update || (this.photoThumbs != null && this.photoThumbs.size() != this.messageOwner.media.photo.sizes.size())) {
                    this.photoThumbs = new ArrayList(this.messageOwner.media.photo.sizes);
                } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty()) {
                    for (a = 0; a < this.photoThumbs.size(); a++) {
                        photoObject = (TLRPC$PhotoSize) this.photoThumbs.get(a);
                        for (b = 0; b < this.messageOwner.media.photo.sizes.size(); b++) {
                            size = (TLRPC$PhotoSize) this.messageOwner.media.photo.sizes.get(b);
                            if (!(size instanceof TLRPC$TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                                photoObject.location = size.location;
                                break;
                            }
                        }
                    }
                }
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
                if (!(this.messageOwner.media.document.thumb instanceof TLRPC$TL_photoSizeEmpty)) {
                    if (!update || this.photoThumbs == null) {
                        this.photoThumbs = new ArrayList();
                        this.photoThumbs.add(this.messageOwner.media.document.thumb);
                    } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty() && this.messageOwner.media.document.thumb != null) {
                        photoObject = (TLRPC$PhotoSize) this.photoThumbs.get(0);
                        photoObject.location = this.messageOwner.media.document.thumb.location;
                        photoObject.f794w = this.messageOwner.media.document.thumb.f794w;
                        photoObject.f793h = this.messageOwner.media.document.thumb.f793h;
                    }
                }
            } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                if (!(this.messageOwner.media.game.document == null || (this.messageOwner.media.game.document.thumb instanceof TLRPC$TL_photoSizeEmpty))) {
                    if (!update) {
                        this.photoThumbs = new ArrayList();
                        this.photoThumbs.add(this.messageOwner.media.game.document.thumb);
                    } else if (!(this.photoThumbs == null || this.photoThumbs.isEmpty() || this.messageOwner.media.game.document.thumb == null)) {
                        ((TLRPC$PhotoSize) this.photoThumbs.get(0)).location = this.messageOwner.media.game.document.thumb.location;
                    }
                }
                if (this.messageOwner.media.game.photo != null) {
                    if (!update || this.photoThumbs2 == null) {
                        this.photoThumbs2 = new ArrayList(this.messageOwner.media.game.photo.sizes);
                    } else if (!this.photoThumbs2.isEmpty()) {
                        for (a = 0; a < this.photoThumbs2.size(); a++) {
                            photoObject = (TLRPC$PhotoSize) this.photoThumbs2.get(a);
                            for (b = 0; b < this.messageOwner.media.game.photo.sizes.size(); b++) {
                                size = (TLRPC$PhotoSize) this.messageOwner.media.game.photo.sizes.get(b);
                                if (!(size instanceof TLRPC$TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                                    photoObject.location = size.location;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (this.photoThumbs == null && this.photoThumbs2 != null) {
                    this.photoThumbs = this.photoThumbs2;
                    this.photoThumbs2 = null;
                }
            } else if (!(this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage)) {
            } else {
                if (this.messageOwner.media.webpage.photo != null) {
                    if (!update || this.photoThumbs == null) {
                        this.photoThumbs = new ArrayList(this.messageOwner.media.webpage.photo.sizes);
                    } else if (!this.photoThumbs.isEmpty()) {
                        for (a = 0; a < this.photoThumbs.size(); a++) {
                            photoObject = (TLRPC$PhotoSize) this.photoThumbs.get(a);
                            for (b = 0; b < this.messageOwner.media.webpage.photo.sizes.size(); b++) {
                                size = (TLRPC$PhotoSize) this.messageOwner.media.webpage.photo.sizes.get(b);
                                if (!(size instanceof TLRPC$TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                                    photoObject.location = size.location;
                                    break;
                                }
                            }
                        }
                    }
                } else if (this.messageOwner.media.webpage.document != null && !(this.messageOwner.media.webpage.document.thumb instanceof TLRPC$TL_photoSizeEmpty)) {
                    if (!update) {
                        this.photoThumbs = new ArrayList();
                        this.photoThumbs.add(this.messageOwner.media.webpage.document.thumb);
                    } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty() && this.messageOwner.media.webpage.document.thumb != null) {
                        ((TLRPC$PhotoSize) this.photoThumbs.get(0)).location = this.messageOwner.media.webpage.document.thumb.location;
                    }
                }
            }
        }
    }

    public CharSequence replaceWithLink(CharSequence source, String param, ArrayList<Integer> uids, AbstractMap<Integer, User> usersDict, SparseArray<User> sUsersDict) {
        if (TextUtils.indexOf(source, param) < 0) {
            return source;
        }
        SpannableStringBuilder names = new SpannableStringBuilder("");
        for (int a = 0; a < uids.size(); a++) {
            User user = null;
            if (usersDict != null) {
                user = (User) usersDict.get(uids.get(a));
            } else if (sUsersDict != null) {
                user = (User) sUsersDict.get(((Integer) uids.get(a)).intValue());
            }
            if (user == null) {
                user = MessagesController.getInstance(this.currentAccount).getUser((Integer) uids.get(a));
            }
            if (user != null) {
                String name = UserObject.getUserName(user);
                int start = names.length();
                if (names.length() != 0) {
                    names.append(", ");
                }
                names.append(name);
                names.setSpan(new URLSpanNoUnderlineBold("" + user.id), start, name.length() + start, 33);
            }
        }
        return TextUtils.replace(source, new String[]{param}, new CharSequence[]{names});
    }

    public CharSequence replaceWithLink(CharSequence source, String param, TLObject object) {
        int start = TextUtils.indexOf(source, param);
        if (start < 0) {
            return source;
        }
        String name;
        String id;
        if (object instanceof User) {
            name = UserObject.getUserName((User) object);
            id = "" + ((User) object).id;
        } else if (object instanceof TLRPC$Chat) {
            name = ((TLRPC$Chat) object).title;
            id = "" + (-((TLRPC$Chat) object).id);
        } else if (object instanceof TLRPC$TL_game) {
            name = ((TLRPC$TL_game) object).title;
            id = "game";
        } else {
            name = "";
            id = "0";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(TextUtils.replace(source, new String[]{param}, new String[]{name.replace('\n', ' ')}));
        builder.setSpan(new URLSpanNoUnderlineBold("" + id), start, name.length() + start, 33);
        return builder;
    }

    public String getExtension() {
        String fileName = getFileName();
        int idx = fileName.lastIndexOf(46);
        String ext = null;
        if (idx != -1) {
            ext = fileName.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0) {
            ext = this.messageOwner.media.document.mime_type;
        }
        if (ext == null) {
            ext = "";
        }
        return ext.toUpperCase();
    }

    public String getFileName() {
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
            return FileLoader.getAttachFileName(this.messageOwner.media.document);
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
            ArrayList<TLRPC$PhotoSize> sizes = this.messageOwner.media.photo.sizes;
            if (sizes.size() > 0) {
                TLRPC$PhotoSize sizeFull = FileLoader.getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                if (sizeFull != null) {
                    return FileLoader.getAttachFileName(sizeFull);
                }
            }
        } else if (this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) {
            return FileLoader.getAttachFileName(this.messageOwner.media.webpage.document);
        }
        return "";
    }

    public int getFileType() {
        if (isVideo()) {
            return 2;
        }
        if (isVoice()) {
            return 1;
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
            return 3;
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
            return 0;
        }
        return 4;
    }

    private static boolean containsUrls(CharSequence message) {
        if (message == null || message.length() < 2 || message.length() > CacheDataSink.DEFAULT_BUFFER_SIZE) {
            return false;
        }
        int length = message.length();
        int digitsInRow = 0;
        int schemeSequence = 0;
        int dotSequence = 0;
        char lastChar = '\u0000';
        int i = 0;
        while (i < length) {
            char c = message.charAt(i);
            if (c >= '0' && c <= '9') {
                digitsInRow++;
                if (digitsInRow >= 6) {
                    return true;
                }
                schemeSequence = 0;
                dotSequence = 0;
            } else if (c == ' ' || digitsInRow <= 0) {
                digitsInRow = 0;
            }
            if ((c == '@' || c == '#' || c == '/' || c == '$') && i == 0) {
                return true;
            }
            if (i != 0 && (message.charAt(i - 1) == ' ' || message.charAt(i - 1) == '\n')) {
                return true;
            }
            if (c == ':') {
                if (schemeSequence == 0) {
                    schemeSequence = 1;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '/') {
                if (schemeSequence == 2) {
                    return true;
                }
                if (schemeSequence == 1) {
                    schemeSequence++;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '.') {
                if (dotSequence != 0 || lastChar == ' ') {
                    dotSequence = 0;
                } else {
                    dotSequence++;
                }
            } else if (c != ' ' && lastChar == '.' && dotSequence == 1) {
                return true;
            } else {
                dotSequence = 0;
            }
            lastChar = c;
            i++;
        }
        return false;
    }

    public void generateLinkDescription() {
        if (this.linkDescription == null) {
            if ((this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) && (this.messageOwner.media.webpage instanceof TLRPC$TL_webPage) && this.messageOwner.media.webpage.description != null) {
                this.linkDescription = Factory.getInstance().newSpannable(this.messageOwner.media.webpage.description);
            } else if ((this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) && this.messageOwner.media.game.description != null) {
                this.linkDescription = Factory.getInstance().newSpannable(this.messageOwner.media.game.description);
            } else if ((this.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice) && this.messageOwner.media.description != null) {
                this.linkDescription = Factory.getInstance().newSpannable(this.messageOwner.media.description);
            }
            if (this.linkDescription != null) {
                if (containsUrls(this.linkDescription)) {
                    try {
                        Linkify.addLinks((Spannable) this.linkDescription, 1);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                this.linkDescription = Emoji.replaceEmoji(this.linkDescription, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            }
        }
    }

    public void generateCaption() {
        if (this.caption == null && !isRoundVideo() && !isMediaEmpty() && !(this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) && !TextUtils.isEmpty(this.messageOwner.message)) {
            boolean hasEntities;
            boolean useManualParse;
            this.caption = Emoji.replaceEmoji(this.messageOwner.message, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            if (this.messageOwner.send_state != 0) {
                hasEntities = false;
                for (int a = 0; a < this.messageOwner.entities.size(); a++) {
                    if (!(this.messageOwner.entities.get(a) instanceof TLRPC$TL_inputMessageEntityMentionName)) {
                        hasEntities = true;
                        break;
                    }
                }
            } else {
                hasEntities = !this.messageOwner.entities.isEmpty();
            }
            if (hasEntities || !(this.eventId != 0 || (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto_old) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto_layer68) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto_layer74) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument_old) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument_layer68) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument_layer74) || ((isOut() && this.messageOwner.send_state != 0) || this.messageOwner.id < 0))) {
                useManualParse = false;
            } else {
                useManualParse = true;
            }
            if (useManualParse) {
                if (containsUrls(this.caption)) {
                    try {
                        Linkify.addLinks((Spannable) this.caption, 5);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                addUsernamesAndHashtags(isOutOwner(), this.caption, true);
            } else {
                try {
                    Linkify.addLinks((Spannable) this.caption, 4);
                } catch (Throwable e2) {
                    FileLog.e(e2);
                }
            }
            addEntitiesToText(this.caption, useManualParse);
        }
    }

    private static void addUsernamesAndHashtags(boolean isOut, CharSequence charSequence, boolean botCommands) {
        try {
            if (urlPattern == null) {
                urlPattern = Pattern.compile("(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s)@[a-zA-Z\\d_]{1,32}|(^|\\s)#[\\w.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)");
            }
            Matcher matcher = urlPattern.matcher(charSequence);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                char ch = charSequence.charAt(start);
                if (!(ch == '@' || ch == '#' || ch == '/' || ch == '$')) {
                    start++;
                }
                URLSpanNoUnderline url = null;
                if (charSequence.charAt(start) != '/') {
                    url = new URLSpanNoUnderline(charSequence.subSequence(start, end).toString());
                } else if (botCommands) {
                    int i;
                    String charSequence2 = charSequence.subSequence(start, end).toString();
                    if (isOut) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    url = new URLSpanBotCommand(charSequence2, i);
                }
                if (url != null) {
                    ((Spannable) charSequence).setSpan(url, start, end, 0);
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static int[] getWebDocumentWidthAndHeight(TLRPC$WebDocument document) {
        if (document == null) {
            return null;
        }
        int a = 0;
        int size = document.attributes.size();
        while (a < size) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TLRPC$TL_documentAttributeImageSize) {
                return new int[]{attribute.f787w, attribute.f786h};
            } else if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                return new int[]{attribute.f787w, attribute.f786h};
            } else {
                a++;
            }
        }
        return null;
    }

    public static int getWebDocumentDuration(TLRPC$WebDocument document) {
        if (document == null) {
            return 0;
        }
        int size = document.attributes.size();
        for (int a = 0; a < size; a++) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                return attribute.duration;
            }
            if (attribute instanceof TLRPC$TL_documentAttributeAudio) {
                return attribute.duration;
            }
        }
        return 0;
    }

    public static int[] getInlineResultWidthAndHeight(TLRPC$BotInlineResult inlineResult) {
        int[] result = getWebDocumentWidthAndHeight(inlineResult.content);
        if (result != null) {
            return result;
        }
        result = getWebDocumentWidthAndHeight(inlineResult.thumb);
        if (result == null) {
            return new int[]{0, 0};
        }
        return result;
    }

    public static int getInlineResultDuration(TLRPC$BotInlineResult inlineResult) {
        int result = getWebDocumentDuration(inlineResult.content);
        if (result == 0) {
            return getWebDocumentDuration(inlineResult.thumb);
        }
        return result;
    }

    public boolean hasValidGroupId() {
        return (getGroupId() == 0 || this.photoThumbs == null || this.photoThumbs.isEmpty()) ? false : true;
    }

    public long getGroupIdForUse() {
        return this.localSentGroupId != 0 ? this.localSentGroupId : this.messageOwner.grouped_id;
    }

    public long getGroupId() {
        return this.localGroupId != 0 ? this.localGroupId : getGroupIdForUse();
    }

    public static void addLinks(boolean isOut, CharSequence messageText) {
        addLinks(isOut, messageText, true);
    }

    public static void addLinks(boolean isOut, CharSequence messageText, boolean botCommands) {
        if ((messageText instanceof Spannable) && containsUrls(messageText)) {
            if (messageText.length() < 1000) {
                try {
                    Linkify.addLinks((Spannable) messageText, 5);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else {
                try {
                    Linkify.addLinks((Spannable) messageText, 1);
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
            }
            addUsernamesAndHashtags(isOut, messageText, botCommands);
        }
    }

    public void resetPlayingProgress() {
        this.audioProgress = 0.0f;
        this.audioProgressSec = 0;
        this.bufferedProgress = 0.0f;
    }

    private boolean addEntitiesToText(CharSequence text, boolean useManualParse) {
        return addEntitiesToText(text, this.messageOwner.entities, isOutOwner(), this.type, true, false, useManualParse);
    }

    public boolean addEntitiesToText(CharSequence text, boolean photoViewer, boolean useManualParse) {
        return addEntitiesToText(text, this.messageOwner.entities, isOutOwner(), this.type, true, photoViewer, useManualParse);
    }

    public static boolean addEntitiesToText(CharSequence text, ArrayList<TLRPC$MessageEntity> entities, boolean out, int type, boolean usernames, boolean photoViewer, boolean useManualParse) {
        boolean hasUrls = false;
        if (!(text instanceof Spannable)) {
            return false;
        }
        byte t;
        Spannable spannable = (Spannable) text;
        int count = entities.size();
        URLSpan[] spans = (URLSpan[]) spannable.getSpans(0, text.length(), URLSpan.class);
        if (spans != null && spans.length > 0) {
            hasUrls = true;
        }
        if (photoViewer) {
            t = (byte) 2;
        } else if (out) {
            t = (byte) 1;
        } else {
            t = (byte) 0;
        }
        int a = 0;
        boolean hasUrls2 = hasUrls;
        while (a < count) {
            TLRPC$MessageEntity entity = (TLRPC$MessageEntity) entities.get(a);
            if (entity.length > 0 && entity.offset >= 0) {
                if (entity.offset >= text.length()) {
                    hasUrls = hasUrls2;
                } else {
                    if (entity.offset + entity.length > text.length()) {
                        entity.length = text.length() - entity.offset;
                    }
                    if ((!useManualParse || (entity instanceof TLRPC$TL_messageEntityBold) || (entity instanceof TLRPC$TL_messageEntityItalic) || (entity instanceof TLRPC$TL_messageEntityCode) || (entity instanceof TLRPC$TL_messageEntityPre) || (entity instanceof TLRPC$TL_messageEntityMentionName) || (entity instanceof TLRPC$TL_inputMessageEntityMentionName)) && spans != null && spans.length > 0) {
                        for (int b = 0; b < spans.length; b++) {
                            if (spans[b] != null) {
                                int start = spannable.getSpanStart(spans[b]);
                                int end = spannable.getSpanEnd(spans[b]);
                                if ((entity.offset <= start && entity.offset + entity.length >= start) || (entity.offset <= end && entity.offset + entity.length >= end)) {
                                    spannable.removeSpan(spans[b]);
                                    spans[b] = null;
                                }
                            }
                        }
                    }
                    if (entity instanceof TLRPC$TL_messageEntityBold) {
                        spannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), entity.offset, entity.offset + entity.length, 33);
                        hasUrls = hasUrls2;
                    } else if (entity instanceof TLRPC$TL_messageEntityItalic) {
                        spannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/ritalic.ttf")), entity.offset, entity.offset + entity.length, 33);
                        hasUrls = hasUrls2;
                    } else if ((entity instanceof TLRPC$TL_messageEntityCode) || (entity instanceof TLRPC$TL_messageEntityPre)) {
                        spannable.setSpan(new URLSpanMono(spannable, entity.offset, entity.offset + entity.length, t), entity.offset, entity.offset + entity.length, 33);
                        hasUrls = hasUrls2;
                    } else if (entity instanceof TLRPC$TL_messageEntityMentionName) {
                        if (usernames) {
                            spannable.setSpan(new URLSpanUserMention("" + ((TLRPC$TL_messageEntityMentionName) entity).user_id, t), entity.offset, entity.offset + entity.length, 33);
                            hasUrls = hasUrls2;
                        }
                    } else if (entity instanceof TLRPC$TL_inputMessageEntityMentionName) {
                        if (usernames) {
                            spannable.setSpan(new URLSpanUserMention("" + ((TLRPC$TL_inputMessageEntityMentionName) entity).user_id.user_id, t), entity.offset, entity.offset + entity.length, 33);
                            hasUrls = hasUrls2;
                        }
                    } else if (!useManualParse) {
                        String url = TextUtils.substring(text, entity.offset, entity.offset + entity.length);
                        if (entity instanceof TLRPC$TL_messageEntityBotCommand) {
                            spannable.setSpan(new URLSpanBotCommand(url, t), entity.offset, entity.offset + entity.length, 33);
                            hasUrls = hasUrls2;
                        } else if ((entity instanceof TLRPC$TL_messageEntityHashtag) || ((usernames && (entity instanceof TLRPC$TL_messageEntityMention)) || (entity instanceof TLRPC$TL_messageEntityCashtag))) {
                            spannable.setSpan(new URLSpanNoUnderline(url), entity.offset, entity.offset + entity.length, 33);
                            hasUrls = hasUrls2;
                        } else if (entity instanceof TLRPC$TL_messageEntityEmail) {
                            spannable.setSpan(new URLSpanReplacement("mailto:" + url), entity.offset, entity.offset + entity.length, 33);
                            hasUrls = hasUrls2;
                        } else if (entity instanceof TLRPC$TL_messageEntityUrl) {
                            if (Browser.isPassportUrl(entity.url)) {
                                hasUrls = hasUrls2;
                            } else {
                                hasUrls = true;
                                if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("tg://")) {
                                    spannable.setSpan(new URLSpanBrowser(url), entity.offset, entity.offset + entity.length, 33);
                                } else {
                                    spannable.setSpan(new URLSpanBrowser("http://" + url), entity.offset, entity.offset + entity.length, 33);
                                }
                            }
                        } else if (entity instanceof TLRPC$TL_messageEntityPhone) {
                            hasUrls = true;
                            String tel = PhoneFormat.stripExceptNumbers(url);
                            if (url.startsWith("+")) {
                                tel = "+" + tel;
                            }
                            spannable.setSpan(new URLSpanBrowser("tel:" + tel), entity.offset, entity.offset + entity.length, 33);
                        } else if (entity instanceof TLRPC$TL_messageEntityTextUrl) {
                            if (Browser.isPassportUrl(entity.url)) {
                                hasUrls = hasUrls2;
                            } else {
                                spannable.setSpan(new URLSpanReplacement(entity.url), entity.offset, entity.offset + entity.length, 33);
                            }
                        }
                    }
                }
                a++;
                hasUrls2 = hasUrls;
            }
            hasUrls = hasUrls2;
            a++;
            hasUrls2 = hasUrls;
        }
        return hasUrls2;
    }

    public void generateLayout(User fromUser) {
        if (this.type == 0 && this.messageOwner.to_id != null && !TextUtils.isEmpty(this.messageText)) {
            boolean hasEntities;
            int a;
            TextPaint paint;
            generateLinkDescription();
            this.textLayoutBlocks = new ArrayList();
            this.textWidth = 0;
            if (this.messageOwner.send_state != 0) {
                hasEntities = false;
                for (a = 0; a < this.messageOwner.entities.size(); a++) {
                    if (!(this.messageOwner.entities.get(a) instanceof TLRPC$TL_inputMessageEntityMentionName)) {
                        hasEntities = true;
                        break;
                    }
                }
            } else {
                hasEntities = !this.messageOwner.entities.isEmpty();
            }
            boolean useManualParse = !hasEntities && (this.eventId != 0 || (this.messageOwner instanceof TLRPC$TL_message_old) || (this.messageOwner instanceof TLRPC$TL_message_old2) || (this.messageOwner instanceof TLRPC$TL_message_old3) || (this.messageOwner instanceof TLRPC$TL_message_old4) || (this.messageOwner instanceof TLRPC$TL_messageForwarded_old) || (this.messageOwner instanceof TLRPC$TL_messageForwarded_old2) || (this.messageOwner instanceof TLRPC$TL_message_secret) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice) || ((isOut() && this.messageOwner.send_state != 0) || this.messageOwner.id < 0 || (this.messageOwner.media instanceof TLRPC$TL_messageMediaUnsupported)));
            if (useManualParse) {
                addLinks(isOutOwner(), this.messageText);
            } else if ((this.messageText instanceof Spannable) && this.messageText.length() < 1000) {
                try {
                    Linkify.addLinks((Spannable) this.messageText, 4);
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
            boolean hasUrls = addEntitiesToText(this.messageText, useManualParse);
            boolean showAvatar = TurboConfig$BG.showAvatar;
            boolean showMyAvatar = TurboConfig$BG.showMyAvatar;
            boolean showMyAvatarGroup = TurboConfig$BG.showMyAvatarGroup;
            boolean needShare = this.eventId == 0 && !isOutOwner() && (!(this.messageOwner.fwd_from == null || (this.messageOwner.fwd_from.saved_from_peer == null && this.messageOwner.fwd_from.from_id == 0 && this.messageOwner.fwd_from.channel_id == 0)) || ((this.messageOwner.from_id > 0 && showMyAvatar && ((this.messageOwner.to_id.channel_id != 0 || this.messageOwner.to_id.chat_id != 0 || (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice)) && showMyAvatarGroup && isOut())) || (this.messageOwner.from_id > 0 && showAvatar && !isOutOwner())));
            this.generatedWithMinSize = AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() : AndroidUtilities.displaySize.x;
            int i = this.generatedWithMinSize;
            float f = (needShare || this.eventId != 0) ? 132.0f : 80.0f;
            int maxWidth = i - AndroidUtilities.dp(f);
            if ((fromUser != null && fromUser.bot) || ((isMegagroup() || !(this.messageOwner.fwd_from == null || this.messageOwner.fwd_from.channel_id == 0)) && !isOut())) {
                maxWidth -= AndroidUtilities.dp(20.0f);
            }
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                maxWidth -= AndroidUtilities.dp(10.0f);
            }
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                paint = Theme.chat_msgGameTextPaint;
            } else {
                paint = Theme.chat_msgTextPaint;
            }
            try {
                StaticLayout textLayout;
                int blocksCount;
                if (VERSION.SDK_INT >= 24) {
                    textLayout = Builder.obtain(this.messageText, 0, this.messageText.length(), paint, maxWidth).setBreakStrategy(1).setHyphenationFrequency(0).setAlignment(Alignment.ALIGN_NORMAL).build();
                } else {
                    textLayout = new StaticLayout(this.messageText, paint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                this.textHeight = textLayout.getHeight();
                if (this.replyMessageObject == null && this.textHeight < AndroidUtilities.dp(40.0f) && ((this.messageOwner.from_id > 0 && showMyAvatar && isOut()) || (((isGroup() || isMegagroup()) && showMyAvatarGroup && isOut()) || (this.messageOwner.from_id > 0 && showAvatar && !isOutOwner())))) {
                    this.textHeight += AndroidUtilities.dp(6.0f);
                }
                this.linesCount = textLayout.getLineCount();
                if (VERSION.SDK_INT >= 24) {
                    blocksCount = 1;
                } else {
                    blocksCount = (int) Math.ceil((double) (((float) this.linesCount) / 10.0f));
                }
                int linesOffset = 0;
                float prevOffset = 0.0f;
                for (a = 0; a < blocksCount; a++) {
                    int currentBlockLinesCount;
                    float lastLeft;
                    float lastLine;
                    if (VERSION.SDK_INT >= 24) {
                        currentBlockLinesCount = this.linesCount;
                    } else {
                        currentBlockLinesCount = Math.min(10, this.linesCount - linesOffset);
                    }
                    TextLayoutBlock block = new TextLayoutBlock();
                    if (blocksCount == 1) {
                        block.textLayout = textLayout;
                        block.textYOffset = 0.0f;
                        block.charactersOffset = 0;
                        block.height = this.textHeight;
                    } else {
                        int startCharacter = textLayout.getLineStart(linesOffset);
                        int endCharacter = textLayout.getLineEnd((linesOffset + currentBlockLinesCount) - 1);
                        if (endCharacter >= startCharacter) {
                            block.charactersOffset = startCharacter;
                            block.charactersEnd = endCharacter;
                            if (hasUrls) {
                                try {
                                    if (VERSION.SDK_INT >= 24) {
                                        block.textLayout = Builder.obtain(this.messageText, startCharacter, endCharacter, paint, AndroidUtilities.dp(2.0f) + maxWidth).setBreakStrategy(1).setHyphenationFrequency(0).setAlignment(Alignment.ALIGN_NORMAL).build();
                                        block.textYOffset = (float) textLayout.getLineTop(linesOffset);
                                        if (a != 0) {
                                            block.height = (int) (block.textYOffset - prevOffset);
                                        }
                                        block.height = Math.max(block.height, block.textLayout.getLineBottom(block.textLayout.getLineCount() - 1));
                                        prevOffset = block.textYOffset;
                                        if (a == blocksCount - 1) {
                                            currentBlockLinesCount = Math.max(currentBlockLinesCount, block.textLayout.getLineCount());
                                            try {
                                                this.textHeight = Math.max(this.textHeight, (int) (block.textYOffset + ((float) block.textLayout.getHeight())));
                                            } catch (Exception e2) {
                                                FileLog.e(e2);
                                            }
                                        }
                                    }
                                } catch (Exception e22) {
                                    FileLog.e(e22);
                                }
                            }
                            block.textLayout = new StaticLayout(this.messageText, startCharacter, endCharacter, paint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                            block.textYOffset = (float) textLayout.getLineTop(linesOffset);
                            if (a != 0) {
                                block.height = (int) (block.textYOffset - prevOffset);
                            }
                            block.height = Math.max(block.height, block.textLayout.getLineBottom(block.textLayout.getLineCount() - 1));
                            prevOffset = block.textYOffset;
                            if (a == blocksCount - 1) {
                                currentBlockLinesCount = Math.max(currentBlockLinesCount, block.textLayout.getLineCount());
                                this.textHeight = Math.max(this.textHeight, (int) (block.textYOffset + ((float) block.textLayout.getHeight())));
                            }
                        }
                    }
                    this.textLayoutBlocks.add(block);
                    try {
                        lastLeft = block.textLayout.getLineLeft(currentBlockLinesCount - 1);
                        if (a == 0 && lastLeft >= 0.0f) {
                            this.textXOffset = lastLeft;
                        }
                    } catch (Exception e222) {
                        lastLeft = 0.0f;
                        if (a == 0) {
                            this.textXOffset = 0.0f;
                        }
                        FileLog.e(e222);
                    }
                    try {
                        lastLine = block.textLayout.getLineWidth(currentBlockLinesCount - 1);
                    } catch (Exception e2222) {
                        lastLine = 0.0f;
                        FileLog.e(e2222);
                    }
                    int linesMaxWidth = (int) Math.ceil((double) lastLine);
                    if (a == blocksCount - 1) {
                        this.lastLineWidth = linesMaxWidth;
                    }
                    int lastLineWidthWithLeft = (int) Math.ceil((double) (lastLine + lastLeft));
                    int linesMaxWidthWithLeft = lastLineWidthWithLeft;
                    if (currentBlockLinesCount > 1) {
                        boolean hasNonRTL = false;
                        float textRealMaxWidth = 0.0f;
                        float textRealMaxWidthWithLeft = 0.0f;
                        for (int n = 0; n < currentBlockLinesCount; n++) {
                            float lineWidth;
                            float lineLeft;
                            try {
                                lineWidth = block.textLayout.getLineWidth(n);
                            } catch (Exception e22222) {
                                FileLog.e(e22222);
                                lineWidth = 0.0f;
                            }
                            if (lineWidth > ((float) (maxWidth + 20))) {
                                lineWidth = (float) maxWidth;
                            }
                            try {
                                lineLeft = block.textLayout.getLineLeft(n);
                            } catch (Exception e222222) {
                                FileLog.e(e222222);
                                lineLeft = 0.0f;
                            }
                            if (lineLeft > 0.0f) {
                                this.textXOffset = Math.min(this.textXOffset, lineLeft);
                                block.directionFlags = (byte) (block.directionFlags | 1);
                                this.hasRtl = true;
                            } else {
                                block.directionFlags = (byte) (block.directionFlags | 2);
                            }
                            if (!hasNonRTL && lineLeft == 0.0f) {
                                try {
                                    if (block.textLayout.getParagraphDirection(n) == 1) {
                                        hasNonRTL = true;
                                    }
                                } catch (Exception e3) {
                                    hasNonRTL = true;
                                }
                            }
                            textRealMaxWidth = Math.max(textRealMaxWidth, lineWidth);
                            textRealMaxWidthWithLeft = Math.max(textRealMaxWidthWithLeft, lineWidth + lineLeft);
                            linesMaxWidth = Math.max(linesMaxWidth, (int) Math.ceil((double) lineWidth));
                            linesMaxWidthWithLeft = Math.max(linesMaxWidthWithLeft, (int) Math.ceil((double) (lineWidth + lineLeft)));
                        }
                        if (hasNonRTL) {
                            textRealMaxWidth = textRealMaxWidthWithLeft;
                            if (a == blocksCount - 1) {
                                this.lastLineWidth = lastLineWidthWithLeft;
                            }
                        } else if (a == blocksCount - 1) {
                            this.lastLineWidth = linesMaxWidth;
                        }
                        this.textWidth = Math.max(this.textWidth, (int) Math.ceil((double) textRealMaxWidth));
                    } else {
                        if (lastLeft > 0.0f) {
                            this.textXOffset = Math.min(this.textXOffset, lastLeft);
                            if (this.textXOffset == 0.0f) {
                                linesMaxWidth = (int) (((float) linesMaxWidth) + lastLeft);
                            }
                            this.hasRtl = blocksCount != 1;
                            block.directionFlags = (byte) (block.directionFlags | 1);
                        } else {
                            block.directionFlags = (byte) (block.directionFlags | 2);
                        }
                        this.textWidth = Math.max(this.textWidth, Math.min(maxWidth, linesMaxWidth));
                    }
                    linesOffset += currentBlockLinesCount;
                }
            } catch (Exception e2222222) {
                FileLog.e(e2222222);
            }
        }
    }

    public boolean isOut() {
        return this.messageOwner.out;
    }

    public boolean isOutOwner() {
        if (!this.messageOwner.out || this.messageOwner.from_id <= 0 || this.messageOwner.post) {
            return false;
        }
        if (this.messageOwner.fwd_from == null) {
            return true;
        }
        int selfUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
        if (getDialogId() == ((long) selfUserId)) {
            if (this.messageOwner.fwd_from.from_id == selfUserId || (this.messageOwner.fwd_from.saved_from_peer != null && this.messageOwner.fwd_from.saved_from_peer.user_id == selfUserId)) {
                return true;
            }
            return false;
        } else if (this.messageOwner.fwd_from.saved_from_peer == null || this.messageOwner.fwd_from.saved_from_peer.user_id == selfUserId) {
            return true;
        } else {
            return false;
        }
    }

    public boolean needDrawAvatar() {
        return (!isFromUser() && this.eventId == 0 && (this.messageOwner.fwd_from == null || this.messageOwner.fwd_from.saved_from_peer == null)) ? false : true;
    }

    public boolean isFromUser() {
        return this.messageOwner.from_id > 0 && !this.messageOwner.post;
    }

    public boolean isUnread() {
        return this.messageOwner.unread;
    }

    public boolean isContentUnread() {
        return this.messageOwner.media_unread;
    }

    public void setIsRead() {
        this.messageOwner.unread = false;
    }

    public int getUnradFlags() {
        return getUnreadFlags(this.messageOwner);
    }

    public static int getUnreadFlags(TLRPC$Message message) {
        int flags = 0;
        if (!message.unread) {
            flags = 0 | 1;
        }
        if (message.media_unread) {
            return flags;
        }
        return flags | 2;
    }

    public void setContentIsRead() {
        this.messageOwner.media_unread = false;
    }

    public int getId() {
        return this.messageOwner.id;
    }

    public static int getMessageSize(TLRPC$Message message) {
        if (message.media == null || message.media.document == null) {
            return 0;
        }
        return message.media.document.size;
    }

    public int getSize() {
        return getMessageSize(this.messageOwner);
    }

    public long getIdWithChannel() {
        long id = (long) this.messageOwner.id;
        if (this.messageOwner.to_id == null || this.messageOwner.to_id.channel_id == 0) {
            return id;
        }
        return id | (((long) this.messageOwner.to_id.channel_id) << 32);
    }

    public int getChannelId() {
        if (this.messageOwner.to_id != null) {
            return this.messageOwner.to_id.channel_id;
        }
        return 0;
    }

    public static boolean shouldEncryptPhotoOrVideo(TLRPC$Message message) {
        if (message instanceof TLRPC$TL_message_secret) {
            if (((message.media instanceof TLRPC$TL_messageMediaPhoto) || isVideoMessage(message)) && message.ttl > 0 && message.ttl <= 60) {
                return true;
            }
            return false;
        } else if (((message.media instanceof TLRPC$TL_messageMediaPhoto) || (message.media instanceof TLRPC$TL_messageMediaDocument)) && message.media.ttl_seconds != 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldEncryptPhotoOrVideo() {
        return shouldEncryptPhotoOrVideo(this.messageOwner);
    }

    public static boolean isSecretPhotoOrVideo(TLRPC$Message message) {
        if (message instanceof TLRPC$TL_message_secret) {
            if (((message.media instanceof TLRPC$TL_messageMediaPhoto) || isRoundVideoMessage(message) || isVideoMessage(message)) && message.ttl > 0 && message.ttl <= 60) {
                return true;
            }
            return false;
        } else if (!(message instanceof TLRPC$TL_message)) {
            return false;
        } else {
            if (((message.media instanceof TLRPC$TL_messageMediaPhoto) || (message.media instanceof TLRPC$TL_messageMediaDocument)) && message.media.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public boolean needDrawBluredPreview() {
        if (this.messageOwner instanceof TLRPC$TL_message_secret) {
            int ttl = Math.max(this.messageOwner.ttl, this.messageOwner.media.ttl_seconds);
            if (ttl <= 0 || (((!(this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) && !isVideo() && !isGif()) || ttl > 60) && !isRoundVideo())) {
                return false;
            }
            return true;
        } else if (!(this.messageOwner instanceof TLRPC$TL_message)) {
            return false;
        } else {
            if (((this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument)) && this.messageOwner.media.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public boolean isSecretMedia() {
        boolean z = true;
        if (this.messageOwner instanceof TLRPC$TL_message_secret) {
            if ((((this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) || isGif()) && this.messageOwner.ttl > 0 && this.messageOwner.ttl <= 60) || isVoice() || isRoundVideo() || isVideo()) {
                return true;
            }
            return false;
        } else if (!(this.messageOwner instanceof TLRPC$TL_message)) {
            return false;
        } else {
            if (!((this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) || (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument)) || this.messageOwner.media.ttl_seconds == 0) {
                z = false;
            }
            return z;
        }
    }

    public static void setUnreadFlags(TLRPC$Message message, int flag) {
        boolean z;
        boolean z2 = true;
        if ((flag & 1) == 0) {
            z = true;
        } else {
            z = false;
        }
        message.unread = z;
        if ((flag & 2) != 0) {
            z2 = false;
        }
        message.media_unread = z2;
    }

    public static boolean isUnread(TLRPC$Message message) {
        return message.unread;
    }

    public static boolean isContentUnread(TLRPC$Message message) {
        return message.media_unread;
    }

    public boolean isMegagroup() {
        return isMegagroup(this.messageOwner);
    }

    public boolean isSavedFromMegagroup() {
        if (this.messageOwner.fwd_from == null || this.messageOwner.fwd_from.saved_from_peer == null || this.messageOwner.fwd_from.saved_from_peer.channel_id == 0) {
            return false;
        }
        return ChatObject.isMegagroup(MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.saved_from_peer.channel_id)));
    }

    public static boolean isMegagroup(TLRPC$Message message) {
        return (message.flags & Integer.MIN_VALUE) != 0;
    }

    public static boolean isOut(TLRPC$Message message) {
        return message.out;
    }

    public long getDialogId() {
        return getDialogId(this.messageOwner);
    }

    public boolean canStreamVideo() {
        TLRPC$Document document = getDocument();
        if (document == null) {
            return false;
        }
        if (SharedConfig.streamAllVideo) {
            return true;
        }
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                return attribute.supports_streaming;
            }
        }
        return false;
    }

    public static long getDialogId(TLRPC$Message message) {
        if (message.dialog_id == 0 && message.to_id != null) {
            if (message.to_id.chat_id != 0) {
                if (message.to_id.chat_id < 0) {
                    message.dialog_id = AndroidUtilities.makeBroadcastId(message.to_id.chat_id);
                } else {
                    message.dialog_id = (long) (-message.to_id.chat_id);
                }
            } else if (message.to_id.channel_id != 0) {
                message.dialog_id = (long) (-message.to_id.channel_id);
            } else if (isOut(message)) {
                message.dialog_id = (long) message.to_id.user_id;
            } else {
                message.dialog_id = (long) message.from_id;
            }
        }
        return message.dialog_id;
    }

    public boolean isSending() {
        return this.messageOwner.send_state == 1 && this.messageOwner.id < 0;
    }

    public boolean isEditing() {
        return this.messageOwner.send_state == 3 && this.messageOwner.id > 0;
    }

    public boolean isSendError() {
        return this.messageOwner.send_state == 2 && this.messageOwner.id < 0;
    }

    public boolean isSent() {
        return this.messageOwner.send_state == 0 || this.messageOwner.id > 0;
    }

    public int getSecretTimeLeft() {
        int secondsLeft = this.messageOwner.ttl;
        if (this.messageOwner.destroyTime != 0) {
            return Math.max(0, this.messageOwner.destroyTime - ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
        }
        return secondsLeft;
    }

    public String getSecretTimeString() {
        if (!isSecretMedia()) {
            return null;
        }
        int secondsLeft = getSecretTimeLeft();
        if (secondsLeft < 60) {
            return secondsLeft + "s";
        }
        return (secondsLeft / 60) + "m";
    }

    public String getDocumentName() {
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument) {
            return FileLoader.getDocumentFileName(this.messageOwner.media.document);
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) {
            return FileLoader.getDocumentFileName(this.messageOwner.media.webpage.document);
        }
        return "";
    }

    public static boolean isStickerDocument(TLRPC$Document document) {
        if (document != null) {
            for (int a = 0; a < document.attributes.size(); a++) {
                if (((TLRPC$DocumentAttribute) document.attributes.get(a)) instanceof TLRPC$TL_documentAttributeSticker) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMaskDocument(TLRPC$Document document) {
        if (document != null) {
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if ((attribute instanceof TLRPC$TL_documentAttributeSticker) && attribute.mask) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVoiceDocument(TLRPC$Document document) {
        if (document != null) {
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (attribute instanceof TLRPC$TL_documentAttributeAudio) {
                    return attribute.voice;
                }
            }
        }
        return false;
    }

    public static boolean isVoiceWebDocument(WebFile webDocument) {
        return webDocument != null && webDocument.mime_type.equals("audio/ogg");
    }

    public static boolean isImageWebDocument(WebFile webDocument) {
        return (webDocument == null || isGifDocument(webDocument) || !webDocument.mime_type.startsWith("image/")) ? false : true;
    }

    public static boolean isVideoWebDocument(WebFile webDocument) {
        return webDocument != null && webDocument.mime_type.startsWith("video/");
    }

    public static boolean isMusicDocument(TLRPC$Document document) {
        if (document != null) {
            int a = 0;
            while (a < document.attributes.size()) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (!(attribute instanceof TLRPC$TL_documentAttributeAudio)) {
                    a++;
                } else if (attribute.voice) {
                    return false;
                } else {
                    return true;
                }
            }
            if (!TextUtils.isEmpty(document.mime_type)) {
                String mime = document.mime_type.toLowerCase();
                if (mime.equals(MimeTypes.AUDIO_FLAC) || mime.equals("audio/ogg") || mime.equals(MimeTypes.AUDIO_OPUS) || mime.equals("audio/x-opus+ogg")) {
                    return true;
                }
                if (mime.equals("application/octet-stream") && FileLoader.getDocumentFileName(document).endsWith(".opus")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVideoDocument(TLRPC$Document document) {
        if (document == null) {
            return false;
        }
        boolean isAnimated = false;
        boolean isVideo = false;
        int width = 0;
        int height = 0;
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                if (attribute.round_message) {
                    return false;
                }
                isVideo = true;
                width = attribute.f787w;
                height = attribute.f786h;
            } else if (attribute instanceof TLRPC$TL_documentAttributeAnimated) {
                isAnimated = true;
            }
        }
        if (isAnimated && (width > 1280 || height > 1280)) {
            isAnimated = false;
        }
        if (!isVideo || isAnimated) {
            return false;
        }
        return true;
    }

    public TLRPC$Document getDocument() {
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) {
            return this.messageOwner.media.webpage.document;
        }
        return this.messageOwner.media != null ? this.messageOwner.media.document : null;
    }

    public static boolean isStickerMessage(TLRPC$Message message) {
        return (message.media == null || message.media.document == null || !isStickerDocument(message.media.document)) ? false : true;
    }

    public static boolean isMaskMessage(TLRPC$Message message) {
        return (message.media == null || message.media.document == null || !isMaskDocument(message.media.document)) ? false : true;
    }

    public static boolean isMusicMessage(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return isMusicDocument(message.media.webpage.document);
        }
        return (message.media == null || message.media.document == null || !isMusicDocument(message.media.document)) ? false : true;
    }

    public static boolean isGifMessage(TLRPC$Message message) {
        return (message.media == null || message.media.document == null || !isGifDocument(message.media.document)) ? false : true;
    }

    public static boolean isRoundVideoMessage(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return isRoundVideoDocument(message.media.webpage.document);
        }
        return (message.media == null || message.media.document == null || !isRoundVideoDocument(message.media.document)) ? false : true;
    }

    public static boolean isPhoto(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return message.media.webpage.photo instanceof TLRPC$TL_photo;
        }
        return message.media instanceof TLRPC$TL_messageMediaPhoto;
    }

    public static boolean isVoiceMessage(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return isVoiceDocument(message.media.webpage.document);
        }
        return (message.media == null || message.media.document == null || !isVoiceDocument(message.media.document)) ? false : true;
    }

    public static boolean isNewGifMessage(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return isNewGifDocument(message.media.webpage.document);
        }
        return (message.media == null || message.media.document == null || !isNewGifDocument(message.media.document)) ? false : true;
    }

    public static boolean isLiveLocationMessage(TLRPC$Message message) {
        return message.media instanceof TLRPC$TL_messageMediaGeoLive;
    }

    public static boolean isVideoMessage(TLRPC$Message message) {
        if (message.media instanceof TLRPC$TL_messageMediaWebPage) {
            return isVideoDocument(message.media.webpage.document);
        }
        return (message.media == null || message.media.document == null || !isVideoDocument(message.media.document)) ? false : true;
    }

    public static boolean isGameMessage(TLRPC$Message message) {
        return message.media instanceof TLRPC$TL_messageMediaGame;
    }

    public static boolean isInvoiceMessage(TLRPC$Message message) {
        return message.media instanceof TLRPC$TL_messageMediaInvoice;
    }

    public static TLRPC$InputStickerSet getInputStickerSet(TLRPC$Message message) {
        if (message.media == null || message.media.document == null) {
            return null;
        }
        Iterator it = message.media.document.attributes.iterator();
        while (it.hasNext()) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) it.next();
            if (attribute instanceof TLRPC$TL_documentAttributeSticker) {
                if (attribute.stickerset instanceof TLRPC$TL_inputStickerSetEmpty) {
                    return null;
                }
                return attribute.stickerset;
            }
        }
        return null;
    }

    public static long getStickerSetId(TLRPC$Document document) {
        if (document == null) {
            return -1;
        }
        int a = 0;
        while (a < document.attributes.size()) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (!(attribute instanceof TLRPC$TL_documentAttributeSticker)) {
                a++;
            } else if (attribute.stickerset instanceof TLRPC$TL_inputStickerSetEmpty) {
                return -1;
            } else {
                return attribute.stickerset.id;
            }
        }
        return -1;
    }

    public String getStrickerChar() {
        if (!(this.messageOwner.media == null || this.messageOwner.media.document == null)) {
            Iterator it = this.messageOwner.media.document.attributes.iterator();
            while (it.hasNext()) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) it.next();
                if (attribute instanceof TLRPC$TL_documentAttributeSticker) {
                    return attribute.alt;
                }
            }
        }
        return null;
    }

    public int getApproximateHeight() {
        if (this.type == 0) {
            int i = this.textHeight;
            int dp = ((this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) && (this.messageOwner.media.webpage instanceof TLRPC$TL_webPage)) ? AndroidUtilities.dp(100.0f) : 0;
            int height = i + dp;
            return isReply() ? height + AndroidUtilities.dp(42.0f) : height;
        } else if (this.type == 2) {
            return AndroidUtilities.dp(72.0f);
        } else {
            if (this.type == 12) {
                return AndroidUtilities.dp(71.0f);
            }
            if (this.type == 9) {
                return AndroidUtilities.dp(100.0f);
            }
            if (this.type == 4) {
                return AndroidUtilities.dp(114.0f);
            }
            if (this.type == 14) {
                return AndroidUtilities.dp(82.0f);
            }
            if (this.type == 10) {
                return AndroidUtilities.dp(30.0f);
            }
            if (this.type == 11) {
                return AndroidUtilities.dp(50.0f);
            }
            if (this.type == 5) {
                return AndroidUtilities.roundMessageSize;
            }
            int photoHeight;
            int photoWidth;
            if (this.type == 13) {
                float maxWidth;
                float maxHeight = ((float) AndroidUtilities.displaySize.y) * 0.4f;
                if (AndroidUtilities.isTablet()) {
                    maxWidth = ((float) AndroidUtilities.getMinTabletSide()) * 0.5f;
                } else {
                    maxWidth = ((float) AndroidUtilities.displaySize.x) * 0.5f;
                }
                photoHeight = 0;
                photoWidth = 0;
                Iterator it = this.messageOwner.media.document.attributes.iterator();
                while (it.hasNext()) {
                    TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) it.next();
                    if (attribute instanceof TLRPC$TL_documentAttributeImageSize) {
                        photoWidth = attribute.f787w;
                        photoHeight = attribute.f786h;
                        break;
                    }
                }
                if (photoWidth == 0) {
                    photoHeight = (int) maxHeight;
                    photoWidth = photoHeight + AndroidUtilities.dp(100.0f);
                }
                if (((float) photoHeight) > maxHeight) {
                    photoWidth = (int) (((float) photoWidth) * (maxHeight / ((float) photoHeight)));
                    photoHeight = (int) maxHeight;
                }
                if (((float) photoWidth) > maxWidth) {
                    photoHeight = (int) (((float) photoHeight) * (maxWidth / ((float) photoWidth)));
                }
                return photoHeight + AndroidUtilities.dp(14.0f);
            }
            if (AndroidUtilities.isTablet()) {
                photoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
            } else {
                photoWidth = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
            }
            photoHeight = photoWidth + AndroidUtilities.dp(100.0f);
            if (photoWidth > AndroidUtilities.getPhotoSize()) {
                photoWidth = AndroidUtilities.getPhotoSize();
            }
            if (photoHeight > AndroidUtilities.getPhotoSize()) {
                photoHeight = AndroidUtilities.getPhotoSize();
            }
            TLRPC$PhotoSize currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
            if (currentPhotoObject != null) {
                int h = (int) (((float) currentPhotoObject.f793h) / (((float) currentPhotoObject.f794w) / ((float) photoWidth)));
                if (h == 0) {
                    h = AndroidUtilities.dp(100.0f);
                }
                if (h > photoHeight) {
                    h = photoHeight;
                } else if (h < AndroidUtilities.dp(120.0f)) {
                    h = AndroidUtilities.dp(120.0f);
                }
                if (needDrawBluredPreview()) {
                    if (AndroidUtilities.isTablet()) {
                        h = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                    } else {
                        h = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.5f);
                    }
                }
                photoHeight = h;
            }
            return photoHeight + AndroidUtilities.dp(14.0f);
        }
    }

    public String getStickerEmoji() {
        int a = 0;
        while (a < this.messageOwner.media.document.attributes.size()) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) this.messageOwner.media.document.attributes.get(a);
            if (!(attribute instanceof TLRPC$TL_documentAttributeSticker)) {
                a++;
            } else if (attribute.alt == null || attribute.alt.length() <= 0) {
                return null;
            } else {
                return attribute.alt;
            }
        }
        return null;
    }

    public boolean isSticker() {
        if (this.type != 1000) {
            return this.type == 13;
        } else {
            return isStickerMessage(this.messageOwner);
        }
    }

    public boolean isMask() {
        return isMaskMessage(this.messageOwner);
    }

    public boolean isMusic() {
        return isMusicMessage(this.messageOwner);
    }

    public boolean isVoice() {
        return isVoiceMessage(this.messageOwner);
    }

    public boolean isVideo() {
        return isVideoMessage(this.messageOwner);
    }

    public boolean isLiveLocation() {
        return isLiveLocationMessage(this.messageOwner);
    }

    public boolean isGame() {
        return isGameMessage(this.messageOwner);
    }

    public boolean isInvoice() {
        return isInvoiceMessage(this.messageOwner);
    }

    public boolean isRoundVideo() {
        if (this.isRoundVideoCached == 0) {
            int i = (this.type == 5 || isRoundVideoMessage(this.messageOwner)) ? 1 : 2;
            this.isRoundVideoCached = i;
        }
        if (this.isRoundVideoCached == 1) {
            return true;
        }
        return false;
    }

    public boolean hasPhotoStickers() {
        return (this.messageOwner.media == null || this.messageOwner.media.photo == null || !this.messageOwner.media.photo.has_stickers) ? false : true;
    }

    public boolean isGif() {
        return isGifMessage(this.messageOwner);
    }

    public boolean isWebpageDocument() {
        return (!(this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage) || this.messageOwner.media.webpage.document == null || isGifDocument(this.messageOwner.media.webpage.document)) ? false : true;
    }

    public boolean isWebpage() {
        return this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage;
    }

    public boolean isNewGif() {
        return this.messageOwner.media != null && isNewGifDocument(this.messageOwner.media.document);
    }

    public String getMusicTitle() {
        return getMusicTitle(true);
    }

    public String getMusicTitle(boolean unknown) {
        TLRPC$Document document;
        if (this.type == 0) {
            document = this.messageOwner.media.webpage.document;
        } else {
            document = this.messageOwner.media.document;
        }
        if (document != null) {
            int a = 0;
            while (a < document.attributes.size()) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (attribute instanceof TLRPC$TL_documentAttributeAudio) {
                    if (!attribute.voice) {
                        String title = attribute.title;
                        if (title != null && title.length() != 0) {
                            return title;
                        }
                        title = FileLoader.getDocumentFileName(document);
                        if (TextUtils.isEmpty(title) && unknown) {
                            return LocaleController.getString("AudioUnknownTitle", R.string.AudioUnknownTitle);
                        }
                        return title;
                    } else if (unknown) {
                        return LocaleController.formatDateAudio((long) this.messageOwner.date);
                    } else {
                        return null;
                    }
                } else if ((attribute instanceof TLRPC$TL_documentAttributeVideo) && attribute.round_message) {
                    return LocaleController.formatDateAudio((long) this.messageOwner.date);
                } else {
                    a++;
                }
            }
            String fileName = FileLoader.getDocumentFileName(document);
            if (!TextUtils.isEmpty(fileName)) {
                return fileName;
            }
        }
        return LocaleController.getString("AudioUnknownTitle", R.string.AudioUnknownTitle);
    }

    public int getDuration() {
        TLRPC$Document document;
        if (this.type == 0) {
            document = this.messageOwner.media.webpage.document;
        } else {
            document = this.messageOwner.media.document;
        }
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TLRPC$TL_documentAttributeAudio) {
                return attribute.duration;
            }
            if (attribute instanceof TLRPC$TL_documentAttributeVideo) {
                return attribute.duration;
            }
        }
        return this.audioPlayerDuration;
    }

    public String getMusicAuthor() {
        return getMusicAuthor(true);
    }

    public String getMusicAuthor(boolean unknown) {
        TLRPC$Document document;
        if (this.type == 0) {
            document = this.messageOwner.media.webpage.document;
        } else {
            document = this.messageOwner.media.document;
        }
        if (document != null) {
            boolean isVoice = false;
            for (int a = 0; a < document.attributes.size(); a++) {
                TLRPC$DocumentAttribute attribute = (TLRPC$DocumentAttribute) document.attributes.get(a);
                if (attribute instanceof TLRPC$TL_documentAttributeAudio) {
                    if (attribute.voice) {
                        isVoice = true;
                    } else {
                        String performer = attribute.performer;
                        if (TextUtils.isEmpty(performer) && unknown) {
                            return LocaleController.getString("AudioUnknownArtist", R.string.AudioUnknownArtist);
                        }
                        return performer;
                    }
                } else if ((attribute instanceof TLRPC$TL_documentAttributeVideo) && attribute.round_message) {
                    isVoice = true;
                }
                if (isVoice) {
                    if (!unknown) {
                        return null;
                    }
                    if (isOutOwner() || (this.messageOwner.fwd_from != null && this.messageOwner.fwd_from.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId())) {
                        return LocaleController.getString("FromYou", R.string.FromYou);
                    }
                    User user = null;
                    TLRPC$Chat chat = null;
                    if (this.messageOwner.fwd_from != null && this.messageOwner.fwd_from.channel_id != 0) {
                        chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.channel_id));
                    } else if (this.messageOwner.fwd_from != null && this.messageOwner.fwd_from.from_id != 0) {
                        user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.fwd_from.from_id));
                    } else if (this.messageOwner.from_id < 0) {
                        chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-this.messageOwner.from_id));
                    } else if (this.messageOwner.from_id != 0 || this.messageOwner.to_id.channel_id == 0) {
                        user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
                    } else {
                        chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.to_id.channel_id));
                    }
                    if (user != null) {
                        return UserObject.getUserName(user);
                    }
                    if (chat != null) {
                        return chat.title;
                    }
                }
            }
        }
        return LocaleController.getString("AudioUnknownArtist", R.string.AudioUnknownArtist);
    }

    public TLRPC$InputStickerSet getInputStickerSet() {
        return getInputStickerSet(this.messageOwner);
    }

    public boolean isForwarded() {
        return isForwardedMessage(this.messageOwner);
    }

    public boolean needDrawForwarded() {
        return ((this.messageOwner.flags & 4) == 0 || this.messageOwner.fwd_from == null || this.messageOwner.fwd_from.saved_from_peer != null || ((long) UserConfig.getInstance(this.currentAccount).getClientUserId()) == getDialogId()) ? false : true;
    }

    public static boolean isForwardedMessage(TLRPC$Message message) {
        return ((message.flags & 4) == 0 || message.fwd_from == null) ? false : true;
    }

    public boolean isReply() {
        return (this.replyMessageObject == null || !(this.replyMessageObject.messageOwner instanceof TLRPC$TL_messageEmpty)) && !((this.messageOwner.reply_to_msg_id == 0 && this.messageOwner.reply_to_random_id == 0) || (this.messageOwner.flags & 8) == 0);
    }

    public boolean isMediaEmpty() {
        return isMediaEmpty(this.messageOwner);
    }

    public static boolean isMediaEmpty(TLRPC$Message message) {
        return message == null || message.media == null || (message.media instanceof TLRPC$TL_messageMediaEmpty) || (message.media instanceof TLRPC$TL_messageMediaWebPage);
    }

    public boolean canEditMessage(TLRPC$Chat chat) {
        return canEditMessage(this.currentAccount, this.messageOwner, chat);
    }

    public boolean canEditMedia() {
        boolean z = true;
        if (isSecretMedia()) {
            return false;
        }
        if (this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) {
            return true;
        }
        if (!(this.messageOwner.media instanceof TLRPC$TL_messageMediaDocument)) {
            return false;
        }
        if (isVoice() || isSticker() || isRoundVideo()) {
            z = false;
        }
        return z;
    }

    public boolean canEditMessageAnytime(TLRPC$Chat chat) {
        return canEditMessageAnytime(this.currentAccount, this.messageOwner, chat);
    }

    public static boolean canEditMessageAnytime(int currentAccount, TLRPC$Message message, TLRPC$Chat chat) {
        if (message == null || message.to_id == null || ((message.media != null && (isRoundVideoDocument(message.media.document) || isStickerDocument(message.media.document))) || ((message.action != null && !(message.action instanceof TLRPC$TL_messageActionEmpty)) || isForwardedMessage(message) || message.via_bot_id != 0 || message.id < 0))) {
            return false;
        }
        if (message.from_id == message.to_id.user_id && message.from_id == UserConfig.getInstance(currentAccount).getClientUserId() && !isLiveLocationMessage(message)) {
            return true;
        }
        if (chat == null && message.to_id.channel_id != 0) {
            chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(Integer.valueOf(message.to_id.channel_id));
            if (chat == null) {
                return false;
            }
        }
        if (message.out && chat != null && chat.megagroup) {
            if (chat.creator) {
                return true;
            }
            if (chat.admin_rights != null && chat.admin_rights.pin_messages) {
                return true;
            }
        }
        return false;
    }

    public static boolean canEditMessage(int currentAccount, TLRPC$Message message, TLRPC$Chat chat) {
        boolean z = true;
        if ((chat != null && (chat.left || chat.kicked)) || message == null || message.to_id == null) {
            return false;
        }
        if (message.media != null && (isRoundVideoDocument(message.media.document) || isStickerDocument(message.media.document))) {
            return false;
        }
        if ((message.action != null && !(message.action instanceof TLRPC$TL_messageActionEmpty)) || isForwardedMessage(message) || message.via_bot_id != 0 || message.id < 0) {
            return false;
        }
        if (message.from_id == message.to_id.user_id && message.from_id == UserConfig.getInstance(currentAccount).getClientUserId() && !isLiveLocationMessage(message) && !(message.media instanceof TLRPC$TL_messageMediaContact)) {
            return true;
        }
        if (chat == null && message.to_id.channel_id != 0) {
            chat = MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(message.to_id.channel_id));
            if (chat == null) {
                return false;
            }
        }
        if (message.media != null && !(message.media instanceof TLRPC$TL_messageMediaEmpty) && !(message.media instanceof TLRPC$TL_messageMediaPhoto) && !(message.media instanceof TLRPC$TL_messageMediaDocument) && !(message.media instanceof TLRPC$TL_messageMediaWebPage)) {
            return false;
        }
        if (message.out && chat != null && chat.megagroup && (chat.creator || (chat.admin_rights != null && chat.admin_rights.pin_messages))) {
            return true;
        }
        if (Math.abs(message.date - ConnectionsManager.getInstance(currentAccount).getCurrentTime()) > MessagesController.getInstance(currentAccount).maxEditTime) {
            return false;
        }
        if (message.to_id.channel_id == 0) {
            if (!((message.out || message.from_id == UserConfig.getInstance(currentAccount).getClientUserId()) && ((message.media instanceof TLRPC$TL_messageMediaPhoto) || (((message.media instanceof TLRPC$TL_messageMediaDocument) && !isStickerMessage(message)) || (message.media instanceof TLRPC$TL_messageMediaEmpty) || (message.media instanceof TLRPC$TL_messageMediaWebPage) || message.media == null)))) {
                z = false;
            }
            return z;
        }
        if (!(chat.megagroup && message.out)) {
            if (chat.megagroup) {
                return false;
            }
            if (!chat.creator) {
                if (chat.admin_rights == null) {
                    return false;
                }
                if (!(chat.admin_rights.edit_messages || message.out)) {
                    return false;
                }
            }
            if (!message.post) {
                return false;
            }
        }
        if ((message.media instanceof TLRPC$TL_messageMediaPhoto) || (((message.media instanceof TLRPC$TL_messageMediaDocument) && !isStickerMessage(message)) || (message.media instanceof TLRPC$TL_messageMediaEmpty) || (message.media instanceof TLRPC$TL_messageMediaWebPage) || message.media == null)) {
            return true;
        }
        return false;
    }

    public boolean canDeleteMessage(TLRPC$Chat chat) {
        return this.eventId == 0 && canDeleteMessage(this.currentAccount, this.messageOwner, chat);
    }

    public static boolean canDeleteMessage(int currentAccount, TLRPC$Message message, TLRPC$Chat chat) {
        boolean z = false;
        if (message.id < 0) {
            return true;
        }
        if (chat == null && message.to_id.channel_id != 0) {
            chat = MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(message.to_id.channel_id));
        }
        if (!ChatObject.isChannel(chat)) {
            if (isOut(message) || !ChatObject.isChannel(chat)) {
                z = true;
            }
            return z;
        } else if (message.post) {
            return true;
        } else {
            if (message.id != 1) {
                if (chat.creator) {
                    return true;
                }
                if (chat.admin_rights != null && (chat.admin_rights.delete_messages || message.out)) {
                    return true;
                }
                if (chat.megagroup && message.out && message.from_id > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getForwardedName() {
        if (this.messageOwner.fwd_from != null) {
            if (this.messageOwner.fwd_from.channel_id != 0) {
                TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.channel_id));
                if (chat != null) {
                    return chat.title;
                }
            } else if (this.messageOwner.fwd_from.from_id != 0) {
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.fwd_from.from_id));
                if (user != null) {
                    return UserObject.getUserName(user);
                }
            }
        }
        return null;
    }

    public int getFromId() {
        if (this.messageOwner.fwd_from == null || this.messageOwner.fwd_from.saved_from_peer == null) {
            if (this.messageOwner.from_id != 0) {
                return this.messageOwner.from_id;
            }
            if (this.messageOwner.post) {
                return this.messageOwner.to_id.channel_id;
            }
        } else if (this.messageOwner.fwd_from.saved_from_peer.user_id != 0) {
            if (this.messageOwner.fwd_from.from_id != 0) {
                return this.messageOwner.fwd_from.from_id;
            }
            return this.messageOwner.fwd_from.saved_from_peer.user_id;
        } else if (this.messageOwner.fwd_from.saved_from_peer.channel_id != 0) {
            if (isSavedFromMegagroup() && this.messageOwner.fwd_from.from_id != 0) {
                return this.messageOwner.fwd_from.from_id;
            }
            if (this.messageOwner.fwd_from.channel_id != 0) {
                return -this.messageOwner.fwd_from.channel_id;
            }
            return -this.messageOwner.fwd_from.saved_from_peer.channel_id;
        } else if (this.messageOwner.fwd_from.saved_from_peer.chat_id != 0) {
            if (this.messageOwner.fwd_from.from_id != 0) {
                return this.messageOwner.fwd_from.from_id;
            }
            if (this.messageOwner.fwd_from.channel_id != 0) {
                return -this.messageOwner.fwd_from.channel_id;
            }
            return -this.messageOwner.fwd_from.saved_from_peer.chat_id;
        }
        return 0;
    }

    public void checkMediaExistance() {
        this.attachPathExists = false;
        this.mediaExists = false;
        File file;
        if (this.type == 1) {
            if (FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize()) != null) {
                file = FileLoader.getPathToMessage(this.messageOwner);
                if (needDrawBluredPreview()) {
                    this.mediaExists = new File(file.getAbsolutePath() + ".enc").exists();
                }
                if (!this.mediaExists) {
                    this.mediaExists = file.exists();
                }
            }
        } else if (this.type == 8 || this.type == 3 || this.type == 9 || this.type == 2 || this.type == 14 || this.type == 5) {
            if (this.messageOwner.attachPath != null && this.messageOwner.attachPath.length() > 0) {
                this.attachPathExists = new File(this.messageOwner.attachPath).exists();
            }
            if (!this.attachPathExists) {
                file = FileLoader.getPathToMessage(this.messageOwner);
                if (this.type == 3 && needDrawBluredPreview()) {
                    this.mediaExists = new File(file.getAbsolutePath() + ".enc").exists();
                }
                if (!this.mediaExists) {
                    this.mediaExists = file.exists();
                }
            }
        } else {
            TLRPC$Document document = getDocument();
            if (document != null) {
                this.mediaExists = FileLoader.getPathToAttach(document).exists();
            } else if (this.type == 0) {
                TLRPC$PhotoSize currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
                if (currentPhotoObject != null && currentPhotoObject != null) {
                    this.mediaExists = FileLoader.getPathToAttach(currentPhotoObject, true).exists();
                }
            }
        }
    }

    public boolean isChat() {
        if (this.messageOwner.to_id.user_id != 0) {
            return true;
        }
        return false;
    }

    public boolean isGroup() {
        if (this.messageOwner.to_id.chat_id != 0) {
            return true;
        }
        return false;
    }

    public boolean isChannel() {
        if (this.messageOwner.to_id.channel_id == 0 || this.messageOwner.from_id >= 0 || !this.messageOwner.post) {
            return false;
        }
        return true;
    }
}
