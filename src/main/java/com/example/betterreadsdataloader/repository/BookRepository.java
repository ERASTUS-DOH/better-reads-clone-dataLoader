package com.example.betterreadsdataloader.repository;

import com.example.betterreadsdataloader.model.Book;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface BookRepository extends CassandraRepository<Book, String> {
}
