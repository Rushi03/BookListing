package com.example.android.booklisting;

/**
 * Created by Rushi Patel on 7/29/2016.
 */
public class Book {
    //Title of the book
    public final String title;

    //Author of the book
    public final String bookAuthor;

    public Book(String resultTitle, String resultAuthor) {
        title = resultTitle;
        bookAuthor = resultAuthor;
    }

    public String getTitle() {
        return title;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }
}
