package com.example.tunispromos;

public class Promotion {
    private String id, title, description, category, providerId, startDate, endDate, imageUrl;
    private double priceBefore, priceAfter;
    public Promotion() {}

    public Promotion(String id, String title, String description, String category, String providerId, String startDate, String endDate, String imageUrl, double priceBefore, double priceAfter) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.providerId = providerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.priceBefore = priceBefore;
        this.priceAfter = priceAfter;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getProviderId() {
        return providerId;
    }
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public double getPriceBefore() {
        return priceBefore;
    }
    public void setPriceBefore(double priceBefore) {
        this.priceBefore = priceBefore;
    }
    public double getPriceAfter() {
        return priceAfter;
    }
    public void setPriceAfter(double priceAfter) {
        this.priceAfter = priceAfter;
    }

}
