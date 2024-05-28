package com.group46.infosys1d;

public class ReviewModel {

    private String userId; // ID of the user being reviewed
    private String reviewerId; // ID of the user leaving the review
    private float rating; // The rating given
    private String comment;

    public void Review(String userId, String reviewerId, float rating, String comment) {
        this.userId = userId;
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}