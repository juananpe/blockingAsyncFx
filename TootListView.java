/*

    Based on: https://github.com/Vuzi/twitter-c
    Apache License 2.0

 */
package eus.ehu.mastoclient.view;

import eus.ehu.mastoclient.utils.Utils;
import eus.ehu.mastoclient.view.component.MastodonMediaVideoView;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller of a toot contained in a toot list view.
 * <p>
 * Based on code by Vuzi
 */
public class TootListView {

    // XML values
    public Pane profileImage;
    public HBox retweetedPanel;
    public Label retweetedBy;
    public Label userName;
    public Label userTag;
    public Label date;
    public TextFlow content;
    public Pane tweetPanel;
    public Button seeDetailButton;
    public Button addFavoriteButton;
    public Button retweetButton;
    public Button respondButton;
    public HBox responseAtPanel;
    public Label responseTo;
    public HBox hasResponsePanel;
    public Label responses;
    public VBox medias;

    // Running values
    private Status status;                 // Status displayed
    private Image userImage;               // Status' author's image
    // private AppController appController;   // Application controller
    private Account author;                   // Status' author
    private Status responseToValue;        // Status answered by the current status

    /**
     * Set the controller.
     *
     * @param controller The parent controller.
     */
//    public void setController(ViewController controller) {
//        this.appController = controller.getAppController();
//    }

    /**
     * Return the view of the controller.
     *
     * @return The view.
     */
    public Node getView() {
        return tweetPanel;
    }

    /**
     * Update the view with the provided status.
     *
     * @param origStatus The status.
     */
    public void update(Status origStatus) {

        this.status = origStatus;
        if (origStatus.getReblog() != null) {
            status = origStatus.getReblog();
        }

        this.author = status.getAccount();

        // Set retweet information
        if (origStatus.getReblog() != null) {
            retweetedPanel.setVisible(true);
            retweetedPanel.setManaged(true);
            retweetedBy.setText(origStatus.getAccount().getDisplayName());
        } else {
            retweetedPanel.setVisible(false);
            retweetedPanel.setManaged(false);
        }

        // Set response information
//        if(status.getInReplyToStatusId() >= 0) {
//            responseAtPanel.setVisible(true);
//            responseAtPanel.setManaged(true);

//            Utils.asyncTask(() -> TwitterClient.client().showStatus(status.getInReplyToStatusId()), replied -> {
//                if(replied != null) {
//                    responseTo.setText("@" + replied.getUser().getScreenName());
//                    responseToValue = replied;
//                }
//            });
//        } else {
//            responseAtPanel.setVisible(false);
//            responseAtPanel.setManaged(false);
//        }

        respondButton.setText("Replies (" + status.getRepliesCount() + ")");

        // Set user real name
        userName.setText(author.getDisplayName());

        // Set user TAG
        userTag.setText("@" + author.getAcct());

        // Set date
        date.setText(status.getCreatedAt().toString());

        // Set content
        content.getChildren().clear();
        updateContent(status.getContent());

        // Set user image
        userImage = null;

        if (author.getAvatar() != null) {
            Utils.asyncTask(() -> new Image(author.getAvatar()), image -> {
                userImage = image;
                BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true);
                BackgroundImage backgroundImage = new BackgroundImage(userImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
                profileImage.setBackground(new Background(backgroundImage));
            });
        }

        retweetButton.setText("Boosted (" + status.getReblogsCount() + ")");

        // Update favorite button
        if (status.isFavourited())
            addFavoriteButton.setText("Remove (" + status.getFavouritesCount() + ")");
        else
            addFavoriteButton.setText("Favorites (" + status.getFavouritesCount() + ")");

        // Update media
        medias.getChildren().clear();
        status.getMediaAttachments().forEach(media -> {
            if (media.getType().equals("image")) {
                Utils.asyncTask(() -> new Image(media.getPreviewUrl()), image -> {
                    if (image != null) {
                        // resize image
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(500);
                        medias.getChildren().add(imageView);
                    }
                });
            } else if (media.getType().equals("video") || media.getType().equals("gifv")) {
                System.out.println(media.getType());
                System.out.println(media.getUrl());
                System.out.println(media.getPreviewUrl());
                Utils.asyncTask(() -> new Image(media.getPreviewUrl()), image -> {
                        if(image != null)
                            medias.getChildren().add(new MastodonMediaVideoView(image, media.getUrl()));
                    });
            }
        });

//        for(ExtendedMediaEntity mediaEntity : status.getExtendedMediaEntities()) {
//            if(mediaEntity.getType().equals("photo")) {
//                Utils.asyncTask(() -> new Image(mediaEntity.getMediaURL()), image -> {
//                    if(image != null)
//                        medias.getChildren().add(new TwitterMediaView(image));
//                });
//            } else if(mediaEntity.getType().equals("video") || mediaEntity.getType().equals("animated_gif")) {
//                // Multiples videos can be given, try to get the better MP4 one
//                ExtendedMediaEntity.Variant selectedVariant = null;
//                for(ExtendedMediaEntity.Variant variant : mediaEntity.getVideoVariants()) {
//                    if(variant.getContentType().equals("video/mp4")) {
//                        if(selectedVariant == null || variant.getBitrate() > selectedVariant.getBitrate())
//                            selectedVariant = variant;
//                    }
//                }
//
//                // If video, show image preview
//                if(selectedVariant != null) {
//                    final ExtendedMediaEntity.Variant finalSelectedVariant = selectedVariant;
//                    Utils.asyncTask(() -> new Image(mediaEntity.getMediaURL()), image -> {
//                        if(image != null)
//                            medias.getChildren().add(new TwitterMediaVideoView(image, "http" + finalSelectedVariant.getUrl().substring(5)));
//                    });
//                }
//            }
//
//        }

        // Set responses information
        // Too much data ?
        /*
        Utils.asyncTask(this::getResponses, statuses -> {
            if(statuses.isEmpty()) {
                hasResponsePanel.setVisible(false);
                hasResponsePanel.setManaged(false);
            } else {
                hasResponsePanel.setVisible(true);
                hasResponsePanel.setManaged(true);

                responses.setText(String.valueOf(statuses.size()));
            }
        });*/
    }

    private List<Status> getResponses() {
        ArrayList<Status> replies = new ArrayList<>();

        try {
//            long id = status.getId();
//            String screenName = status.getUser().getScreenName();
//
//            Query query = new Query("@" + screenName + " since_id:" + id);
//            query.setCount(100);
//
//            replies.addAll(TwitterClient.client().search(query)
//                    .getTweets().stream()
//                    .filter(status -> status.getInReplyToStatusId() == id)
//                    .collect(Collectors.toList()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return replies;
    }

    /**
     * Update the toot content using the provided string.
     *
     * @param text The toot content.
     */
    private void updateContent(String text) {
        for (String element : Utils.parseToot(text)) {
            if (element.isEmpty())
                continue;

            Text textElement = new Text();

            // User tag
            if (element.startsWith("@")) {
                textElement.setText(element);
                textElement.setFill(Color.LIGHTBLUE);
                textElement.setOnMouseEntered(event -> textElement.setStyle("-fx-text-fill: darkblue; -fx-cursor: hand"));
                textElement.setOnMouseExited(event -> textElement.setStyle("-fx-text-fill: lightblue; -fx-cursor: inherit"));
//                textElement.setOnMouseClicked(event -> Utils.asyncTask(() -> TwitterClient.client().showUser(element.substring(1)), user -> {
//                    if (user != null)
//                        Utils.showProfilePage(appController, user);
//                }));
            }
            // HashTags
            else if (element.startsWith("#")) {
                textElement.setText(element);
                textElement.setFill(Color.GRAY);
                textElement.setOnMouseEntered(event -> textElement.setStyle("-fx-text-fill: lightgray; -fx-cursor: hand"));
                textElement.setOnMouseExited(event -> textElement.setStyle("-fx-text-fill: gray; -fx-cursor: inherit"));
//                textElement.setOnMouseClicked(event ->
//                        appController.createWindow("Recherche", "SearchView.fxml", Collections.singletonMap("filter", element)));
            }
            // URLs
            else {
                if (element.startsWith("http://") || element.startsWith("https://")) {
                    textElement.setText(element);
                    textElement.setFill(Color.LIGHTGREEN);
                    textElement.setOnMouseEntered(event -> textElement.setStyle("-fx-text-fill: darkgreen; -fx-cursor: hand"));
                    textElement.setOnMouseExited(event -> textElement.setStyle("-fx-text-fill: lightgreen; -fx-cursor: inherit"));
                    textElement.setOnMouseClicked(event -> Utils.openWebPage(element));
                }
                // Regular text
                else {
                    textElement.setText(element);
                }
            }

            content.getChildren().add(textElement);
        }
    }

    /**
     * Action when the "respond" button is clicked.
     */
//    public void respondAction() {
//        Utils.showNewTweetPage(appController, status);
//    }

    /**
     * Action when the "retweet" button is clicked.
     */
    public void retweetAction() {

        if (status == null)
            return;

//        Utils.asyncTask(() -> TwitterClient.client().retweetStatus(status.getId()), status -> {
//            ((TwitterCController) appController).showNotification("Retweet", "Retweet effectué avec succès !");
//
//            this.status = status;
//            update(status);
//        });
    }

    /**
     * Action when the "favorite" button is clicked.
     */
    public void addFavoriteAction() {

        if (status == null)
            return;

        // if(status.isFavorited()) {
//            Utils.asyncTask(() -> TwitterClient.client().destroyFavorite(status.getId()), status -> {
//                ((TwitterCController) appController).showNotification("Confirmation favoris",
//                        "Suppression des favoris du tweet de " + status.getUser().getName() + " effectué avec succès !");
//
//                this.status = status;
//                update(status);
//            });
        // } else {
//            Utils.asyncTask(() -> TwitterClient.client().createFavorite(status.getId()), status -> {
//                ((TwitterCController) appController).showNotification("Confirmation favoris",
//                        "Mise en favoris du tweet de " + status.getUser().getName() + " effectué avec succès !");
//
//                this.status = status;
//                update(status);
//            });
        //   }
    }

    /**
     * Action when the "see" button is clicked.
     */
//    public void seeDetailAction() {
//        Utils.showTweetPage(appController, status);
//    }

    /**
     * Action when the user name is clicked.
     */
//    public void showUserAction() {
//        Utils.showProfilePage(appController, author);
//    }

    /**
     * Action when the retweet user name is clicked.
     */
//    public void showRetweetUserAction() {
//        Utils.showProfilePage(appController, status.getUser());
//    }

    /**
     * Action when the response to label is clicked.
     */
    public void showResponseTweetAction() {
//        if(responseToValue != null)
//            Utils.showTweetPage(appController, responseToValue);
    }

    /**
     * Action when the response author to label is clicked.
     */
    public void showResponseUserAction() {
//        if(responseToValue != null)
//            Utils.showProfilePage(appController, responseToValue.getUser());
    }

    public void seeTweetOnTweeter() {
        //   Utils.openWebPage("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
    }
}
