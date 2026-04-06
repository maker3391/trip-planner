package com.fiveguys.trip_planner.dto;

public class TourApiPlaceCandidate {

    private String title;
    private String addr1;
    private String addr2;
    private String firstImage;
    private String contentId;

    public TourApiPlaceCandidate() {
    }

    public TourApiPlaceCandidate(String title, String addr1, String addr2, String firstImage, String contentId) {
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.firstImage = firstImage;
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(String firstImage) {
        this.firstImage = firstImage;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}