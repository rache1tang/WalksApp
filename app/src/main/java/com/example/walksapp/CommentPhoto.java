package com.example.walksapp;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("CommentPhoto")
public class CommentPhoto extends ParseObject {

    public static final String KEY_WALK = "walk";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_FILE = "file";

    public Walk getWalk() {
        return (Walk) getParseObject(KEY_WALK);
    }

    public void setWalk(Walk walk) {
        put(KEY_WALK, walk);
    }

    public Comment getComment() {
        return (Comment) getParseObject(KEY_COMMENT);
    }

    public void setComment(Comment comment) {
        put(KEY_COMMENT, comment);
    }

    public ParseFile getFile() {
        return getParseFile(KEY_FILE);
    }

    public void setFile(ParseFile file) {
        put(KEY_FILE, file);
    }
}
