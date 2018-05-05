package com.example.android.bakingapp.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by john on 28/03/18.
 */

public class Review {


    //    id              integer             optional
    //    page            integer             optional
    //    results         array[Review]       optional
    //        id              string              optional
    //        author          string              optional
    //        content         string              optional
    //        url             string              optional
    //    total_pages         integer         optional
    //    total_results       integer         optional

    private String author;

    private String content;

    private String url;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Video{" +
                "author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public static class Page {

        // page            integer         optional
        // results         array[Movie]    optional
        // total_results   integer         optional
        // total_pages     integer         optional

        private Integer page;
        private List<Review> results;
        @SerializedName("total_results")
        private Integer totalResults;
        @SerializedName("total_pages")
        private Integer totalPages;

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public List<Review> getResults() {
            return results;
        }

        public void setResults(List<Review> results) {
            this.results = results;
        }

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        @Override
        public String toString() {
            return "Page{" +
                    "page=" + page +
                    ", results=" + results +
                    ", totalResults=" + totalResults +
                    ", totalPages=" + totalPages +
                    '}';
        }
    }
}
