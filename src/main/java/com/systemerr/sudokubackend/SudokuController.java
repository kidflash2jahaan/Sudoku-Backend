package com.systemerr.sudokubackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@RestController
public class SudokuController {
    private final MongoTemplate mongodb;

    public SudokuController(MongoTemplate mongodb) {
        this.mongodb = mongodb;
    }

    @GetMapping("/users/{user}/new")
    public SudokuBoard createGame(@PathVariable String user, @RequestParam(value = "difficulty", defaultValue = "Easy") String newDifficulty) {
        SudokuAPIResponse response = null;
        String currentDifficulty = "";

        if (!newDifficulty.equals("Easy") && !newDifficulty.equals("Medium") && !newDifficulty.equals("Hard")) {
            switch (newDifficulty) {
                case "medium", "2" -> newDifficulty = "Medium";
                case "hard", "3" -> newDifficulty = "Hard";
                default -> newDifficulty = "Easy";
            }
        }

        while (!currentDifficulty.equals(newDifficulty)) {
            response = sudokuAPI();
            currentDifficulty = response.newboard().grids().getFirst().getDifficulty();
        }

        SudokuBoard board = new SudokuBoard(user, response.newboard().grids().getFirst().getValue(), response.newboard().grids().getFirst().getSolution(), response.newboard().grids().getFirst().getDifficulty());

        mongodb.insert(board);
        return board;
    }

    @GetMapping("/users/{user}/games")
    public List<SudokuBoard> getGames(@PathVariable String user) {
        return mongodb.find(Query.query(Criteria.where("user").is(user)), SudokuBoard.class, "boards");
    }

    @GetMapping("/games/{id}")
    public SudokuBoard getGame(@PathVariable String id) {
        return mongodb.findById(new ObjectId(id), SudokuBoard.class);
    }
    
    @PatchMapping("/games/{id}")
    public SudokuBoard updateGame(@PathVariable String id, @RequestBody int[][] value) {
        SudokuBoard game = getGame(id);
        game.setValue(value);
        
        mongodb.remove(getGame(id));
        mongodb.insert(game);
        
        return game;
    }
    
    @PostMapping("/users/new")
    public User createUser(@RequestBody User user) {
        User newUser = new User(user.getUsername(), user.getPassword());
        
        mongodb.insert(newUser);
        return newUser;
    }
    
    @GetMapping("/users/{user}")
    public User getUser(@PathVariable String user) {
        return mongodb.findById(user, User.class);
    }

    public SudokuAPIResponse sudokuAPI() {
        StringBuilder response = new StringBuilder();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://sudoku-api.vercel.app/api/dosuku").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return new ObjectMapper().readValue(response.toString(), SudokuAPIResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
