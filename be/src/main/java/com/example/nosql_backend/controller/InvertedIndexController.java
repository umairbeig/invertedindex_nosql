package com.example.nosql_backend.controller;
import com.example.nosql_backend.entity.tablestruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.Arrays;
import java.util.Comparator;



@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class InvertedIndexController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/search/{word}/{selectedValue}")
    public ResponseEntity<tablestruct.Doc[]> searchWord(@PathVariable String word, @PathVariable String selectedValue) {
        System.out.println("The entered word is: " + word);
        System.out.println("The selected value is: " + selectedValue);

        // Specify the collection name based on the selected value
        String collectionName = selectedValue;

        Query query = new Query(Criteria.where("word").is(word));
        tablestruct invertedIndex = mongoTemplate.findOne(query, tablestruct.class, collectionName);

        if (invertedIndex != null) {
            tablestruct.Doc[] docs = invertedIndex.getDocs();
            Arrays.sort(docs, Comparator.comparingInt(tablestruct.Doc::getCount).reversed());
            return ResponseEntity.ok().body(docs);
        }

        System.out.println("Word not found");
        return ResponseEntity.notFound().build();
    }
}