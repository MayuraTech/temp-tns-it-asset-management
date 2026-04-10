package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Generic paginated response wrapper for API endpoints.
 * Provides consistent pagination structure across the application.
 * 
 * @param <T> the type of content in the page
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    
    private List<T> content;
    private PageInfo page;
    private Map<String, String> links;
    
    public PageResponse() {
    }
    
    public PageResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }
    
    public PageResponse(List<T> content, PageInfo page, Map<String, String> links) {
        this.content = content;
        this.page = page;
        this.links = links;
    }
    
    // Getters and setters
    
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public PageInfo getPage() {
        return page;
    }
    
    public void setPage(PageInfo page) {
        this.page = page;
    }
    
    public Map<String, String> getLinks() {
        return links;
    }
    
    public void setLinks(Map<String, String> links) {
        this.links = links;
    }
    
    /**
     * Page metadata information.
     */
    public static class PageInfo {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;
        
        public PageInfo() {
        }
        
        public PageInfo(int size, int number, long totalElements, int totalPages) {
            this.size = size;
            this.number = number;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
        
        public int getSize() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }
        
        public int getNumber() {
            return number;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
    
    /**
     * Builder for creating PageResponse instances.
     */
    public static class Builder<T> {
        private final PageResponse<T> response;
        
        public Builder() {
            this.response = new PageResponse<>();
        }
        
        public Builder<T> content(List<T> content) {
            response.content = content;
            return this;
        }
        
        public Builder<T> page(PageInfo page) {
            response.page = page;
            return this;
        }
        
        public Builder<T> page(int size, int number, long totalElements, int totalPages) {
            response.page = new PageInfo(size, number, totalElements, totalPages);
            return this;
        }
        
        public Builder<T> links(Map<String, String> links) {
            response.links = links;
            return this;
        }
        
        public PageResponse<T> build() {
            return response;
        }
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}
