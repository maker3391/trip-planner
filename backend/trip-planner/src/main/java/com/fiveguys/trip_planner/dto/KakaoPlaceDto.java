package com.fiveguys.trip_planner.dto;

public class KakaoPlaceDto {

    private String placeId;
    private String placeName;
    private String categoryName;
    private String addressName;
    private String roadAddressName;
    private String phone;
    private String placeUrl;
    private String longitude;
    private String latitude;

    public KakaoPlaceDto() {
    }

    public KakaoPlaceDto(String placeId, String placeName, String categoryName,
                         String addressName, String roadAddressName, String phone,
                         String placeUrl, String longitude, String latitude) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.categoryName = categoryName;
        this.addressName = addressName;
        this.roadAddressName = roadAddressName;
        this.phone = phone;
        this.placeUrl = placeUrl;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getRoadAddressName() {
        return roadAddressName;
    }

    public void setRoadAddressName(String roadAddressName) {
        this.roadAddressName = roadAddressName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlaceUrl() {
        return placeUrl;
    }

    public void setPlaceUrl(String placeUrl) {
        this.placeUrl = placeUrl;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}