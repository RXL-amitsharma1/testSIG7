package com.rxlogix.dto

public class RequestCommentDTO {

    List<CommentDTO> requestComment;
    boolean faersFlag;
    boolean vaersFlag;
    boolean vigibaseFlag;
    boolean successFlag;

    public boolean isSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(boolean successFlag) {
        this.successFlag = successFlag;
    }

    public boolean isFaersFlag() {
        return faersFlag;
    }
    public boolean isVaersFlag() {
        return vaersFlag;
    }

    public void setFaersFlag(boolean faersFlag) {
        this.faersFlag = faersFlag;
    }

    public void setVaersFlag(boolean vaersFlag) {
        this.vaersFlag = vaersFlag;
    }

    public boolean isVigibaseFlag() {
        return vigibaseFlag;
    }

    public void setVigibaseFlag(boolean vigibaseFlag) {
        this.vigibaseFlag = vigibaseFlag;
    }

    public List<CommentDTO> getCommentDTOS() {
        return requestComment;
    }

    public void setCommentDTOS(List<CommentDTO> commentDTOS) {
        this.requestComment = commentDTOS;
    }
}
