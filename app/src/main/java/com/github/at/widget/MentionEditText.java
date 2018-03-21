package com.github.at.widget;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.github.at.model.TextExtraStruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zlove on 2018/3/18.
 */

public class MentionEditText extends AppCompatEditText {

    private Runnable mAction;

    private int mMentionTextColor;

    private boolean mIsSelected;
    private Range mLastSelectedRange;
    private List<Range> mRangeArrayList;

    private OnMentionInputListener mOnMentionInputListener;

    public MentionEditText(Context context) {
        super(context);
        init();
    }

    public MentionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MentionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRangeArrayList = new ArrayList<>(5);
        mMentionTextColor = Color.RED;
        addTextChangedListener(new MentionTextWatcher());
    }

    public boolean isContains(int id) {
        Editable spannableText = getText();
        if (spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            return false;
        }
        MentionSpan[] oldSpans = spannableText.getSpans(0, spannableText.length(), MentionSpan.class);
        for (MentionSpan oldSpan : oldSpans) {
            if (oldSpan.mId == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * set listener for mention character('@')
     *
     * @param onMentionInputListener MentionEditText.OnMentionInputListener
     */
    public void setOnMentionInputListener(OnMentionInputListener onMentionInputListener) {
        mOnMentionInputListener = onMentionInputListener;
    }

    /**
     * set highlight color of mention string
     *
     * @param color value from 'getResources().getColor()' or 'Color.parseColor()' etc.
     */
    public void setMentionTextColor(int color) {
        mMentionTextColor = color;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection superInputConnection = super.onCreateInputConnection(outAttrs);
        if (superInputConnection == null) {
            return null;
        }
        return new HackInputConnection(superInputConnection, true, this);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //avoid infinite recursion after calling setSelection()
        if (mLastSelectedRange != null && mLastSelectedRange.isEqual(selStart, selEnd)) {
            return;
        }

        //if user cancel a selection of mention string, reset the state of 'mIsSelected'
        Range closestRange = getRangeOfClosestMentionString(selStart, selEnd);
        if (closestRange != null && closestRange.to == selEnd) {
            mIsSelected = false;
        }

        Range nearbyRange = getRangeOfNearbyMentionString(selStart, selEnd);
        //if there is no mention string nearby the cursor, just skip
        if (nearbyRange == null) {
            return;
        }

        //forbid cursor located in the mention string.
        if (selStart == selEnd) {
            setSelection(nearbyRange.getAnchorPosition(selStart));
        } else {
            if (selEnd < nearbyRange.to) {
                setSelection(selStart, nearbyRange.to);
            }
            if (selStart > nearbyRange.from) {
                setSelection(nearbyRange.from, selEnd);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        colorMentionString();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        //hack, put the cursor at the end of text after calling setText() method
        if (mAction == null) {
            mAction = new Runnable() {
                @Override
                public void run() {
                    setSelection(getText().length());
                }
            };
        }
        post(mAction);
    }

    private void colorMentionString() {
        mIsSelected = false;
        if (mRangeArrayList != null) {
            mRangeArrayList.clear();
        }

        Editable spannableText = getText();
        if (spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            return;
        }
        MentionSpan[] oldSpans = spannableText.getSpans(0, spannableText.length(), MentionSpan.class);
        for (MentionSpan oldSpan : oldSpans) {
            Range range = new Range(spannableText.getSpanStart(oldSpan), spannableText.getSpanEnd(oldSpan));
            mRangeArrayList.add(range);
        }
    }

    public boolean addMentionText(int id, String text) {
        int index = getSelectionStart();
        Editable editableText = getEditableText();
        if (editableText == null) {
            return false;
        }

        // 从已经添加的 @ 中找有没有已经 @ 过的
        SpannableString spannableString = new SpannableString("@" + text + " ");
        MentionSpan newSpan = new MentionSpan(mMentionTextColor, id, spannableString.toString());

        MentionSpan[] existingSpans = getMentionText();
        if (existingSpans != null && Arrays.asList(existingSpans).contains(newSpan)) {
            return false;
        }
        if (TextUtils.isEmpty(editableText)) {
            spannableString.setSpan(newSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            getText().insert(0, spannableString);
            return true;
        }
        int length = editableText.length();
        if (index <= length && index >= 0) {
            if (index > 0 && TextUtils.equals(editableText.subSequence(index - 1, index), "@")) {
                getText().delete(index - 1, index);
                index--;
                length--;
            }
            spannableString.setSpan(newSpan
                    , 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = Math.max(0, index);
            index = Math.min(length, index);
            getText().insert(index, spannableString);
        }
        return true;
    }

    public void removeSpace() {
        Editable editable = getText();
        String str = editable.toString();
        int len = str.length();
        int start = 0;
        while (start < len && str.charAt(start) <= ' ') {
            start++;
        }

        int end = len;
        while (end > start && str.charAt(end - 1) <= ' ') {
            end--;
        }
        if (start < end) {
            if (end + 1 < len) {
                editable.delete(end + 1, len);
            }
            editable.delete(0, start);
        } else {
            setText("");
        }
    }

    public int getMentionTextCount() {
        MentionSpan[] spans = getMentionText();
        return spans == null ? 0 : spans.length;
    }

    public MentionSpan[] getMentionText() {
        Editable spannableText = getText();
        if (spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            return null;
        }
        MentionSpan[] spans = spannableText.getSpans(0, spannableText.length(), MentionSpan.class);
        return spans;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        String text = "";
        if (id == android.R.id.cut || id == android.R.id.copy) {
            int min = 0;
            int max = getText().length();

            if (isFocused()) {
                final int selStart = getSelectionStart();
                final int selEnd = getSelectionEnd();

                min = Math.max(0, Math.min(selStart, selEnd));
                max = Math.max(0, Math.max(selStart, selEnd));
            }
            text = getText().subSequence(min, max).toString();
        }
        boolean consumed = super.onTextContextMenuItem(id);
        if (id == android.R.id.cut || id == android.R.id.copy) {
            setClip(text);
        }
        return consumed;
    }

    private void setClip(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(text);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        MentionSavedState savedState = new MentionSavedState(parcelable);
        savedState.mText = getText().toString();
        savedState.mSelectionEnd = getSelectionEnd();
        savedState.mStructs = getTextExtraStructList();
        return savedState;
    }

    public List<TextExtraStruct> getTextExtraStructList() {
        Editable spannableText = getText();
        if (spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            return null;
        }
        List<TextExtraStruct> textExtraStructList = new ArrayList<>();
        MentionSpan[] oldSpans = spannableText.getSpans(0, spannableText.length(), MentionSpan.class);
        for (MentionSpan oldSpan : oldSpans) {
            oldSpan.mStruct.setStart(spannableText.getSpanStart(oldSpan));
            oldSpan.mStruct.setEnd(spannableText.getSpanEnd(oldSpan) - 1);
            textExtraStructList.add(oldSpan.mStruct);
        }
        return textExtraStructList;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof MentionSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        MentionSavedState savedState = (MentionSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setText(savedState.mText);

        int selectedIndex = Math.min(savedState.mSelectionEnd, getText().length());
        setSelection(selectedIndex);
        setTextExtraList(savedState.mStructs);
    }

    public void setTextExtraList(List<TextExtraStruct> structList) {
        mIsSelected = false;
        if (mRangeArrayList != null) {
            mRangeArrayList.clear();
        }
        if (structList == null || structList.isEmpty()) {
            return;
        }
        Editable spannableText = getText();
        if (spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            return;
        }
        int len = spannableText.length();
        for (TextExtraStruct struct : structList) {
            if (struct.getStart() > len || struct.getEnd() + 1 > len) {
                break;
            }
            MentionSpan what = new MentionSpan(mMentionTextColor, struct.getUserId(), spannableText.subSequence(struct.getStart(), struct.getEnd() + 1).toString());
            spannableText.setSpan(what, struct.getStart(), struct.getEnd() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Range range = new Range(struct.getStart(), struct.getEnd() + 1);
            mRangeArrayList.add(range);
        }
    }

    private OnKeyListener mNewKeyListener;

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        mNewKeyListener = l;
        super.setOnKeyListener(l);
    }

    static class MentionSavedState extends BaseSavedState {
        private static final String TEXT_EXTRA_STRUCT = "text_extra_struct";

        String mText;
        int mSelectionEnd;
        List<TextExtraStruct> mStructs;

        MentionSavedState(Parcelable superState) {
            super(superState);
        }

        private MentionSavedState(Parcel in) {
            super(in);
            mText = in.readString();
            mSelectionEnd = in.readInt();
            Bundle bundle = in.readBundle(getClass().getClassLoader());
            if (bundle != null) {
                mStructs = bundle.getParcelableArrayList(TEXT_EXTRA_STRUCT);
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(mText);
            out.writeInt(mSelectionEnd);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(TEXT_EXTRA_STRUCT, (ArrayList<? extends Parcelable>) mStructs);
            out.writeBundle(bundle);
        }

        public static final Creator<MentionSavedState> CREATOR = new Creator<MentionSavedState>() {
            @Override
            public MentionSavedState createFromParcel(Parcel source) {
                return new MentionSavedState(source);
            }

            @Override
            public MentionSavedState[] newArray(int size) {
                return new MentionSavedState[size];
            }
        };
    }

    //text watcher for mention character('@')
    private class MentionTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count == 1 && !TextUtils.isEmpty(s)) {
                char mentionChar = s.toString().charAt(start);
                if ('@' == mentionChar && mOnMentionInputListener != null) {
                    mOnMentionInputListener.onMentionCharacterInput();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private class HackInputConnection extends InputConnectionWrapper {
        private EditText editText;

        public HackInputConnection(InputConnection target, boolean mutable, MentionEditText editText) {
            super(target, mutable);
            this.editText = editText;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (mNewKeyListener != null) {
                return mNewKeyListener.onKey(MentionEditText.this, event.getKeyCode(), event);
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                int selectionStart = editText.getSelectionStart();
                int selectionEnd = editText.getSelectionEnd();
                Range closestRange = getRangeOfClosestMentionString(selectionStart, selectionEnd);
                if (closestRange == null) {
                    mIsSelected = false;
                    return super.sendKeyEvent(event);
                }
                //if mention string has been selected or the cursor is at the beginning of mention string, just use default action(delete)
                if (mIsSelected || selectionStart == closestRange.from) {
                    mIsSelected = false;
                    //the selected mention string is going to be delete
                    return super.sendKeyEvent(event);
                } else {
                    //select the mention string
                    mIsSelected = true;
                    mLastSelectedRange = closestRange;
                    setSelection(closestRange.to, closestRange.from);
                }
                return true;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            if (beforeLength < 0) {
                int length = -afterLength;
                afterLength = -beforeLength;
                beforeLength = length;
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    private Range getRangeOfClosestMentionString(int selStart, int selEnd) {
        if (mRangeArrayList == null) {
            return null;
        }
        for (Range range : mRangeArrayList) {
            if (range.contains(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    private Range getRangeOfNearbyMentionString(int selStart, int selEnd) {
        if (mRangeArrayList == null) {
            return null;
        }
        for (Range range : mRangeArrayList) {
            if (range.isWrappedBy(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    /**
     * Listener for '@' character
     */
    public interface OnMentionInputListener {
        /**
         * call when '@' character is inserted into EditText
         */
        void onMentionCharacterInput();
    }

    public static class MentionSpan extends ForegroundColorSpan {

        private int mId;
        private String mText;
        private TextExtraStruct mStruct;

        public MentionSpan(@ColorInt int color, int id, String text) {
            super(color);
            mId = id;
            mText = text;
            mStruct = new TextExtraStruct();
            mStruct.setUserId(id);
        }

        public void setText(String text) {
            this.mText = text;
        }

        public String getText() {
            return mText;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mId);
            dest.writeString(this.mText);
            dest.writeParcelable(mStruct, flags);
        }

        protected MentionSpan(Parcel in) {
            super(in);
            this.mId = in.readInt();
            this.mText = in.readString();
            this.mStruct = in.readParcelable(TextExtraStruct.class.getClassLoader());
        }

        public static final Creator<MentionSpan> CREATOR = new Creator<MentionSpan>() {
            @Override
            public MentionSpan createFromParcel(Parcel source) {
                return new MentionSpan(source);
            }

            @Override
            public MentionSpan[] newArray(int size) {
                return new MentionSpan[size];
            }
        };
    }
}
