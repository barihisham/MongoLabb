package sample;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.session.ClientSession;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.print.Doc;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 *
 * Your implementation should access a real database.
 *
 * @author anderslm@kth.se
 */
public class BooksDb implements BooksDbInterface {


    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> booksCollection;
    private MongoCollection<Document> authorsCollection;
    private MongoCursor<Document> cursor;
    private Document docToFind;
    private Document authorDoc;
    private Document bookDoc;
    private List<Book> books;
    private Book book;
    private MongoCursor<Document> authorCursor;
    private List<ObjectId> Ids = new ArrayList<>();
    private List<Author> authors = new ArrayList<>();

    @Override
    public boolean connect(String database) throws IOException, MongoException {
        this.mongoClient = new MongoClient("localhost", 27017);
        this.database = mongoClient.getDatabase("mylib");
        this.booksCollection = this.database.getCollection("books");
        this.authorsCollection = this.database.getCollection("authors");
        return false;
    }

    @Override
    public void disconnect() throws IOException, MongoException {
        mongoClient.close();
        authorCursor.close();
        cursor.close();
    }

    private List<Book> searchResult(MongoCursor<Document> cursor)
    {
        books = new ArrayList<>();
        book = null;

        while(cursor.hasNext()){
            docToFind = (Document) cursor.next();
            this.book = new Book(docToFind.getString("ISBN"),
                    docToFind.getString("title"),
                    Date.valueOf(docToFind.getString("published")),
                    BookGenre.valueOf(docToFind.getString("genre").toUpperCase()),
                    Integer.parseInt(docToFind.getString("rating")));
            this.books.add(book);

            Ids = (List<ObjectId>) docToFind.get("authors");

            for(int i = 0; i < Ids.size(); i++){
                authorCursor = (MongoCursor<Document>) authorsCollection.find(eq("_id", new ObjectId(Ids.get(i).toString()))).iterator();
                while (authorCursor.hasNext()){
                    authorDoc = (Document)authorCursor.next();
                    this.book.addAuthor(new Author((authorDoc.getObjectId("_id").toString()),
                            authorDoc.getString("firstName"),
                            authorDoc.getString("lastName"),
                            Date.valueOf(authorDoc.getString("dateOfBirth"))));
                }
            }
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) throws IOException, MongoException {

/*        books = new ArrayList<>();
        book = null;
        authors = new ArrayList<>();
        Cursor tempAuthorCursor;*/
        //this.docToFind = new BasicDBObject("")
/*        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        };*/
        //AggregateIterable<Document> result =
/*         authorsCollection.aggregate(Arrays.asList(
                 Aggregates.match(or(eq("firstName", Pattern.compile(Author)), eq("lastName", Pattern.compile(Author))))
        )).forEach(printBlock);*/

/*        booksCollection.aggregate(Arrays.asList(
                Aggregates.match(or(eq("title", "whatever"), eq("title", "and the mountains echoed")))
                //Aggregates.project(eq("name", eq()))

        )).forEach(printBlock);*/
        MongoCursor tempCursor;
        Document tempDoc;
        List<Book> tempBooks = new ArrayList<>();
        author = author.toLowerCase();
        tempCursor = authorsCollection.find(or(eq("firstName", Pattern.compile(author)), eq("lastName", Pattern.compile(author)))).iterator();

        while(tempCursor.hasNext()){
            tempDoc = (Document) tempCursor.next();
            cursor = booksCollection.find(eq("authors", tempDoc.get("_id"))).iterator();
            List<Book> resultBooks = this.searchResult(cursor);
            for(int i = 0; i < resultBooks.size();i++){
                if(!tempBooks.contains(resultBooks.get(i))){
                    tempBooks.add(resultBooks.get(i));
                }
            }
        }
        return tempBooks;
    }

    @Override
    public List<Book> searchBooksByTitle(String title) throws IOException, MongoException {

        docToFind = new Document("title", Pattern.compile(title.toLowerCase()));
        // QUERY DONE HERE
        this.cursor = booksCollection.find(docToFind).iterator();
        return this.searchResult(this.cursor);
    }

    @Override
    public List<Book> searchBooksByISBN(String ISBN) throws IOException, MongoException {
        this.docToFind = new Document("ISBN", Pattern.compile(ISBN));
        this.cursor = booksCollection.find(docToFind).iterator();
        return this.searchResult(this.cursor);
    }

    @Override
    public List<Book> searchBooksByRating(String rating) throws IOException, MongoException {
        this.docToFind = new Document("rating", rating);
        this.cursor = booksCollection.find(docToFind).iterator();
        return this.searchResult(this.cursor);
    }

    @Override
    public List<Book> searchBooksByGenre(String genre) throws IOException, MongoException {
        this.docToFind = new Document("genre", Pattern.compile(genre.toUpperCase()));
        this.cursor = booksCollection.find(docToFind).iterator();
        return this.searchResult(this.cursor);
    }

    @Override
    public boolean addBookToDb(Book book) throws IOException, MongoException {

        Document author;
        List<ObjectId> authorIds = new ArrayList<>();
        if(!this.isBookInDb(book.getISBN())) {
            for (int i = 0; i < book.getAuthors().size(); i++) {
                if (!authorExist(book.getAuthors().get(i).getFirstName(), book.getAuthors().get(i).getLastName())) {
                    author = new Document("firstName", book.getAuthors().get(i).getFirstName())
                            .append("lastName", book.getAuthors().get(i).getLastName())
                            .append("dateOfBirth", book.getAuthors().get(i).getDob().toString());
                    authorsCollection.insertOne(author);
                    authorIds.add((ObjectId) author.get("_id"));
                } else {
                    this.authorCursor = authorsCollection.find(and(eq("firstName", book.getAuthors().get(i).getFirstName())
                            , eq("lastName", book.getAuthors().get(i).getLastName()))).iterator();
                    authorIds.add((ObjectId) this.authorCursor.next().get("_id"));
                }
            }
        }

        Document document = new Document("ISBN", book.getISBN())
                .append("title", book.getTitle())
                .append("published", book.getPublished().toString())
                .append("genre", book.getGenre().toString())
                .append("rating", Integer.toString(book.getRating()))
                .append("authors", authorIds);
        booksCollection.insertOne(document);

        return false; //?
    }

    private boolean authorExist(String firstName, String lastName){
        this.authorCursor = authorsCollection.find(and(eq("firstName", firstName), eq("lastName", lastName))).iterator();
        return this.authorCursor.hasNext();
    }

    @Override
    public boolean isBookInDb(String ISBN) throws IOException, MongoException {
        this.authorCursor = authorsCollection.find(eq("ISBN", ISBN)).iterator();
        return authorCursor.hasNext();
    }

    @Override
    public List<Author> searchAuthorById(String ID) throws IOException, MongoException {
        return null;
    }

    @Override
    public List<Author> searchAuthorByName(String name) throws IOException, MongoException {
        return null;
    }
}
