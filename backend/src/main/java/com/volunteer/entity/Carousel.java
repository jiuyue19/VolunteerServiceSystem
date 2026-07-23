package com.volunteer.entity;

import java.time.LocalDateTime;

public class Carousel {
    private Long id;
    private String title;
    private String image;
    private String link;
    private Long activityId;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Carousel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", link='" + link + '\'' +
                ", activityId=" + activityId +
                ", sort=" + sort +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}