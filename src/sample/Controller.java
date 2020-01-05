package sample;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.MongoException;
import javafx.application.Platform;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller{

    private final BooksPane booksView; // view
    private final BooksDbInterface booksDb; // model

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    protected void onSearchSelected(String searchFor, SearchMode mode) 
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try {
                    if (searchFor != null && searchFor.length() > 0) 
                    {
                        List<Book> result = null;
                        switch (mode) 
                        {
                            case Title:
                                result = booksDb.searchBooksByTitle(searchFor);
                                break;
                            case ISBN:
                                result = booksDb.searchBooksByISBN(searchFor);
                                break;
                            case Author:
                                result = booksDb.searchBooksByAuthor(searchFor);
                                System.out.println("Confirm");
                                break;
                            case Rating:
                                result = booksDb.searchBooksByRating(searchFor);
                                break;
                            case Genre:
                                result = booksDb.searchBooksByGenre(searchFor);
                                break;
//                            case ID: 
//                                System.out.println("Confirm id");
//                                result = booksDb.searchBooksbyAuthorID(searchFor);
//                                System.out.println("finish id");
//                                break;
                            default:
                        }
                        if (result == null || result.isEmpty()) 
                        {
                            Platform.runLater(new Runnable() 
                            {
                                @Override
                                public void run() 
                                {
                                    booksView.showAlertAndWait("No results found.", INFORMATION);
                                }
                            });
                        } 
                        else 
                        {
                            final List<Book> finalRes = result;
                            // kan skrivas såhär också
                            Platform.runLater(() -> { booksView.displayBooks(finalRes);});
                        }
                    } 
                    else 
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Enter a search string!", WARNING);});
                    }
                    } catch (IOException | MongoException e)
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Database error.",ERROR);});
                    }
                }
        }.start();
    }
    
    
    protected void onSearchSelectedAuthor(String searchFor, AuthorSearchMode mode)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try {
                    if (searchFor != null && searchFor.length() > 0) 
                    {
                        List<Author> result = null;
                        switch (mode) 
                        {
                            case ID:
                                result = booksDb.searchAuthorById(searchFor);
                                break;
                            case Name:
                                result = booksDb.searchAuthorByName(searchFor);
                                break;
                            default:
                        }
                        if (result == null || result.isEmpty()) 
                        {
                            Platform.runLater(new Runnable() 
                            {
                                @Override
                                public void run() 
                                {
                                    booksView.showAlertAndWait("No results found.", INFORMATION);
                                }
                            });
                        } 
                        else 
                        {
                            final List<Author> finalRes = result;
                            // kan skrivas såhär också
                            Platform.runLater(() -> { booksView.displayAuthors(finalRes);});
                        }
                    } 
                    else 
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Enter a search string!", WARNING);});
                    }
                    } catch (IOException | MongoException e)
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Database error.",ERROR);});
                    }
                }
        }.start();
    }
    
    
    
    
    
    protected void handleAddBookToDb()
    {
        Optional<Book> result = booksView.getDialog().showAndWait();
        new Thread()
        {
            @Override
            public void run()
            {
                if(result.isPresent())
                {
                    try 
                    {
                        booksDb.addBookToDb(result.get());
                        Platform.runLater(() -> { booksView.showAlertAndWait("Book added!", INFORMATION);});
                    } 
                    catch(com.mongodb.MongoWriteException e)
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Book already exist in database", WARNING);});
                    }
                    catch (IOException | MongoException e)
                    {
                        Platform.runLater(() -> { booksView.showAlertAndWait("Database error.",ERROR);});
                    }
                }
            }
        }.start();
    }
    
    
    
    
    
    

    // TODO:
    // Add methods for all types of user interaction (e.g. via  menus).

}
