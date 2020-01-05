package sample;

import com.mongodb.MongoException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 * 
 * @author anderslm@kth.se
 */
public interface BooksDbInterface {
    
    /**
     * Connect to the database.
     * @param database
     * @return true on successful connection.
     */
    public boolean connect(String database) throws IOException, MongoException;
    
    public void disconnect() throws IOException, MongoException;
    
    public List<Book> searchBooksByTitle(String title) throws IOException, MongoException;
    
    public List<Book> searchBooksByISBN(String ISBN) throws IOException, MongoException;
    
    public List<Book> searchBooksByAuthor(String Author) throws IOException, MongoException;
    
    public List<Book> searchBooksByRating(String Rating) throws IOException, MongoException;
    
    public List<Book> searchBooksByGenre(String Genre) throws IOException, MongoException;
    
    public boolean addBookToDb(Book book) throws IOException, MongoException;
    
    public boolean isBookInDb(String ISBN)throws IOException, MongoException;
    
    public List<Author> searchAuthorById(String ID) throws IOException, MongoException;
    
    public List<Author> searchAuthorByName(String name) throws IOException, MongoException;
    //public List<Book> searchBooksbyAuthorID(String ID) throws IOException, SQLException;
    
    // TODO: Add abstract methods for all inserts, deletes and queries 
    // mentioned in the instructions for the assignement.
}
