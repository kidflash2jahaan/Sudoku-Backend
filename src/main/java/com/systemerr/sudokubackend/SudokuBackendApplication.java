package com.systemerr.sudokubackend;

import com.mongodb.client.MongoClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class SudokuBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SudokuBackendApplication.class, args);
    }
}
