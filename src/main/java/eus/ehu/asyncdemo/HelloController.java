package eus.ehu.asyncdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;

public class HelloController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private ImageView imageView;

    @FXML
    private TextArea userInput;
    
    @FXML
    private Spinner<Integer> delaySpinner;
    
    @FXML
    public void initialize() {
        // Ensure spinner has a value factory
        delaySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3, 1));
    }

    @FXML
    protected void onLoadImageAsyncButtonClick() {
        int delay = delaySpinner.getValue();
        welcomeText.setText("Loading image asynchronously... (" + delay + "s delay)");
        userInput.clear();
        
        // Clear the current image to indicate a new operation is starting
        imageView.setImage(null);
        
        // Using asyncTask to load the image in the background
        Utils.asyncTask(() -> new Image("https://ikasten.io/image.php?d=" + delay), image -> {
            imageView.setImage(image);
            welcomeText.setText("Image loaded successfully (async)!");
        });
    }

    @FXML
    protected void onLoadImageBlockingButtonClick() {
        int delay = delaySpinner.getValue();
        
        // EDUCATIONAL NOTE:
        // The line below sets the text of the welcomeText label, BUT...
        // this change won't be visible to the user immediately!
        welcomeText.setText("Loading image synchronously... (" + delay + "s delay)");
        userInput.clear();
        
        // Clear the current image to indicate a new operation is starting
        // NOTE: Just like with the welcomeText update, this won't be visible until after
        // the blocking operation completes due to the same UI thread blocking issue
        imageView.setImage(null);
        
        // IMPORTANT UI THREADING CONCEPT:
        // In JavaFX (and most UI frameworks), UI updates don't happen instantly when you change properties.
        // When you call setText() above, you're not directly updating what's on screen.
        // Instead, JavaFX:
        // 1. Schedules the change to be rendered during the next UI render cycle (called a "pulse")
        // 2. The actual rendering to screen happens later in the JavaFX application thread
        // 3. But before JavaFX can perform that render cycle, we call new Image(...) below
        // 4. This image loading operation blocks the JavaFX application thread completely
        // 5. While the thread is blocked, no UI updates can be processed
        // 6. The "Loading..." text change never gets rendered until after the image loading completes
        // 7. By that time, we've already set the text to "Image loaded successfully"
        //
        // This is why you won't see the "Loading..." message at all with the blocking approach,
        // even though the code to set that text runs before the image loading starts.
        
        // This demonstrates a blocking operation that will freeze the UI
        Image image = new Image("https://ikasten.io/image.php?d=" + delay);
        imageView.setImage(image);
        welcomeText.setText("Image loaded successfully (blocking)!");
    }
}