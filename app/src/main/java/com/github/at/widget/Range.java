package com.github.at.widget;

/**
 * Created by zlove on 2018/3/18.
 */

public class Range {
    int from;
    int to;

    public Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public boolean isWrappedBy(int start, int end) {
        return (start > from && start < to) && (end > from && end < to);
    }

    public boolean contains(int start, int end) {
        int newStart = Math.min(start, end);
        int newEnd = Math.max(start, end);
        return from < newStart && to >= newEnd;
    }

    public boolean isEqual(int start, int end) {
        return (from == start && to == end) || (from == end && to == start);
    }

    public int getAnchorPosition(int value) {
        if ((value - from) - (to - value) >= 0) {
            return to;
        } else {
            return from;
        }
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
