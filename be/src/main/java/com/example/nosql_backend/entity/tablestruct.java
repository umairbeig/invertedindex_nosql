package com.example.nosql_backend.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


public class tablestruct {
    private String id;

    private String word;
    private Doc[] docs;

    public tablestruct(String id, String word, Doc[] docs) {
        this.id = id;
        this.word = word;
        this.docs = docs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Doc[] getDocs() {
        return docs;
    }

    public void setDocs(Doc[] docs) {
        this.docs = docs;
    }

    // Constructor, getters, and setters
    // ...

    public static class Doc {
        private String filename;
        private int count;

        public Doc(String filename, int count) {
            this.filename = filename;
            this.count = count;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        // Constructor, getters, and setters
        // ...
    }
}




