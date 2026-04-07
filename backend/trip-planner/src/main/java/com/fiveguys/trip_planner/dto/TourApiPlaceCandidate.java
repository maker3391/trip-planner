package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tour API 장소 후보 정보 객체")
public class TourApiPlaceCandidate {

    @Schema(description = "장소 제목(명칭)", example = "한라산 국립공원")
    private String title;

    @Schema(description = "기본 주소", example = "제주특별자치도 제주시 1100로")
    private String addr1;

    @Schema(description = "상세 주소", example = "관음사 탐방로")
    private String addr2;

    @Schema(description = "대표 이미지 URL", example = "https://example.com/images/hallasan.jpg")
    private String firstImage;

    @Schema(description = "관광지 고유 콘텐츠 ID", example = "123456")
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