package com.example.betterreadsdataloader.service;


import com.example.betterreadsdataloader.model.Author;
import com.example.betterreadsdataloader.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository){
        this.authorRepository = authorRepository;
    }


    @PostConstruct
    public void init(){
        Author testAuthor = new Author();
        testAuthor.setId("test");
        testAuthor.setName("testName");
        testAuthor.setPersonalName("testPersonalName");
        authorRepository.save(testAuthor);
    }
}
