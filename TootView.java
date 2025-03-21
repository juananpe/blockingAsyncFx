package eus.ehu.mastoclient.view;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import eus.ehu.mastoclient.FxController;
import eus.ehu.mastoclient.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import social.bigbone.MastodonClient;
import social.bigbone.api.Pageable;
import social.bigbone.api.Range;
import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * New toot view. Show an empty tweet with an editable content.
 *
 * Based on code by Vuzi
 */
public class TootView implements FxController {

    // XML values
    public VBox tweet;
    public VBox replies;

    public static final String ID = "TWEET-VIEW";
    private Status status;

    private ObservableList<Status> tootList;   // List of tweets
    private ObservableList<Status> repliesList;  // List of replies

    @FXML
    public void initialize() {

        List<Status> itemList = getStatusesForTimeline();

         tootList = FXCollections.observableList(itemList);

        // Map our list to the Vbox children
        Utils.mapByValue(tootList, tweet.getChildren(), status -> {
            try {
                // Load XML with custom loader
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/eus/ehu/mastoclient/view/TootListView.fxml"));
                //fxmlLoader.setClassLoader(this.cachingClassLoader);
                fxmlLoader.load();

                // Get controller & update
                TootListView controller = fxmlLoader.getController();
             //   controller.setController(this);
                controller.update(status);
                return controller.getView();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Error case
            return null;
        });
//
//        // Prepare the tweet list
//        repliesList = FXCollections.observableArrayList();
//
//        // Map our list to the Vbox children
//        Utils.mapByValue(repliesList, replies.getChildren(), status -> {
//            try {
//                // Load XML with custom loader
//                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fr/esgi/twitterc/view/TweetListView.fxml"));
//                //fxmlLoader.setClassLoader(this.cachingClassLoader);
//                fxmlLoader.load();
//
//                // Get controller & update
//                TweetListView controller = fxmlLoader.getController();
//                controller.setController(this);
//                controller.update(status);
//                return controller.getView();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // Error case
//            return null;
//        });
    }



    private List<Status> getStatusesForTimeline()  {

        List<Status> list;

        MastodonClient client = new MastodonClient.Builder("mastodon.social").accessToken(System.getenv("TOKEN")).build();
        try {
            Pageable<Status> listPage = client.accounts().getStatuses(System.getenv("ACCOUNT_ID")).execute();
            return listPage.getPart();
        } catch (BigBoneRequestException e) {
            throw new RuntimeException(e);
        }


        // String body = readFile("mastodon.json");
        // String test = request("accounts/verify_credentials");
        // String test = request("accounts/lookup?acct=juananpe@mastodon.social");
        // {"id":"109842111446764244","username":"juananpe","acct":"juananpe","display_name":"juanan","locked":false,"bot":false,"discoverable":false,"group":false,"created_at":"2023-02-10T00:00:00.000Z","note":"","url":"https://mastodon.social/@juananpe","avatar":"https://files.mastodon.social/accounts/avatars/109/842/111/446/764/244/original/477693b424a0dee4.jpeg","avatar_static":"https://files.mastodon.social/accounts/avatars/109/842/111/446/764/244/original/477693b424a0dee4.jpeg","header":"https://files.mastodon.social/accounts/headers/109/842/111/446/764/244/original/e9a410d134a56739.jpeg","header_static":"https://files.mastodon.social/accounts/headers/109/842/111/446/764/244/original/e9a410d134a56739.jpeg","followers_count":23,"following_count":39,"statuses_count":4,"last_status_at":"2023-02-24","noindex":false,"emojis":[],"roles":[],"fields":[]}
        // String test = request("accounts/"+id+"/following");

        /*
        String id = "109842111446764244";
        String body = Utils.request("accounts/"+id+"/statuses");
        System.out.println(body);

        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(body, JsonArray.class);

        Type statusList = new TypeToken<ArrayList<Status>>() {}.getType();
        List<Status> list = gson.fromJson(jsonArray.getAsJsonArray(), statusList);


        return list;

        */

    }

    public void showList() {


    }


    /**
     * Update the user information using the provided user.
     *
     * @param status The user.
     */
    private void updateInfo(Status status) {

        // Save status
        this.status = status;

        // Set the first status
        tootList.setAll(status);

        // Set responses
        // Utils.asyncTask(() -> getResponses(20), repliesList::setAll);
    }


}
