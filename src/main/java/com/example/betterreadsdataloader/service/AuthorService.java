package com.example.betterreadsdataloader.service;


import com.example.betterreadsdataloader.model.Author;
import com.example.betterreadsdataloader.repository.AuthorRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class AuthorService {

    @Value("${dataDump.location.author}")
    private String authorDumpLocation;

    @Value("${dataDump.location.works}")
    private String worksDumpLocation;

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository){
        this.authorRepository = authorRepository;
    }


    public void initAuthors(){
        //get the path and read the file.
        Path path = Paths.get(authorDumpLocation);
        try(Stream<String> lines = Files.lines(path)){
            //Fetch each of the files.
            lines.forEach(line-> {
                //read and pass each line
                String jsonString = line.substring(line.indexOf("{"));
                try {
                    //convert each line into a json object.
                    JSONObject jsonObject = new JSONObject(jsonString);
                    //create new objects and persist into the database.
                    Author author = new Author();
                    author.setName(jsonObject.optString("name"));
                    author.setPersonalName(jsonObject.optString("personal_name"));
                    author.setId(jsonObject.optString("key").replace("/authors/", ""));
                    authorRepository.save(author);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initWorks(){

    }

    @PostConstruct
    public void start(){
        initAuthors();
        initWorks();
    }
}
