package com.example.betterreadsdataloader.service;


import com.example.betterreadsdataloader.model.Author;
import com.example.betterreadsdataloader.model.Book;
import com.example.betterreadsdataloader.repository.AuthorRepository;
import com.example.betterreadsdataloader.repository.BookRepository;
import org.json.JSONArray;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AuthorService {

    private static final Logger logger = Logger.getLogger(AuthorService.class.getName());
    @Value("${dataDump.location.author}")
    private String authorDumpLocation;

    @Value("${dataDump.location.works}")
    private String worksDumpLocation;

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository){
        this.authorRepository = authorRepository;
        this.bookRepository= bookRepository;
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
                    this.authorRepository.save(author);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initWorks(){
        Path path = Paths.get(worksDumpLocation);
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        try(Stream<String> lines = Files.lines(path)){
            logger.info("Read and parse each line into a json object.");
            lines.map(line -> line.substring(line.indexOf("{"))).forEach(jsonString -> {
                try {
                    logger.info("started creating books");
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Book book = new Book();
                    logger.info("getting id");
                    book.setId(jsonObject.optString("key").replace("/works/", ""));
                    logger.info("started creating title");
                    book.setName(jsonObject.optString("title"));

                    logger.info("started creating description");
                    JSONObject optionalDescription = jsonObject.optJSONObject("description");
                    if (optionalDescription != null) {
                        book.setDescription(optionalDescription.optString("description"));
                    }

                    logger.info("started creating published date");
                    JSONObject optionalDatePublished = jsonObject.optJSONObject("created");
                    if (optionalDatePublished != null) {
                        String dateString = optionalDatePublished.optString("value");
                        book.setPublishedDate(LocalDate.parse(dateString, dateTimeFormat));
                    }

                    logger.info("started getting covers");
                    JSONArray optionalCoversArray = jsonObject.optJSONArray("covers");
                    if (optionalCoversArray != null) {
                        List<String> coverIds = new ArrayList<>();
                        for (int i = 0; i < optionalCoversArray.length(); i++) {
                            coverIds.add(optionalCoversArray.getString(i));
                        }
                        book.setCoverIds(coverIds);
                    }

                    logger.info("started getting authors");
                    JSONArray optionalAuthorIdArray = jsonObject.getJSONArray("authors");
                    if(optionalAuthorIdArray != null){
                        List<String> authorIds = new ArrayList<>();
                        for(int i = 0; i < optionalAuthorIdArray.length(); i++){
                            String authorId = optionalAuthorIdArray.getJSONObject(i).getJSONObject("author").getString("key").replace("/authors/","");
                            authorIds.add(authorId);
                        }
                        book.setAuthorIds(authorIds);

                        logger.info("started getting authors names");
                        List<String> authorNames = authorIds.stream().map(authorRepository::findById).map(optionalAuthor ->{
                            if(optionalAuthor.isPresent()){
                                return optionalAuthor.get().getName();
                            }else return "Unknown Author";
                        }).collect(Collectors.toList());
                        book.setAuthorNames(authorNames);
                        logger.info("Saving books into the table.");
                        this.bookRepository.save(book);

                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }catch (IOException e){
            throw new RuntimeException();
        }

    }

    @PostConstruct
    public void start(){
//        initAuthors();
        initWorks();
    }
}
