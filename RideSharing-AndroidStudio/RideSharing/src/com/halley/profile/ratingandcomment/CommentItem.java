package com.halley.profile.ratingandcomment;

/**
 * Created by enclaveit on 5/20/15.
 */
public class CommentItem {
    private String comment, comment_about_user_id;

    public String getComment_about_user_id() {
        return comment_about_user_id;
    }

    public void setComment_about_user_id(String comment_about_user_id) {
        this.comment_about_user_id = comment_about_user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
