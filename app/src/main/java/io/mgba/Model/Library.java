package io.mgba.Model;

import android.util.Log;
import com.annimon.stream.Stream;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import io.mgba.Data.Database.Game;
import io.mgba.Data.Platform;
import io.mgba.Data.Remote.DTOs.GameJSON;
import io.mgba.Model.IO.Decoder;
import io.mgba.Model.IO.FilesManager;
import io.mgba.Model.Interfaces.IDatabase;
import io.mgba.Model.Interfaces.IFilesManager;
import io.mgba.Model.Interfaces.ILibrary;
import io.mgba.mgba;
import io.reactivex.Single;

public class Library implements ILibrary {
    private static final String TAG = "ProcService";
    private final mgba application;
    @Inject IDatabase database;
    @Inject IFilesManager filesService;

    public Library(mgba application) {
        this.application = application;
        application.inject(this);
    }

    @Override
    public Single<List<Game>> prepareGames(Platform platform) {
        return Single.create(subscriber -> {

            if(platform == null){
                subscriber.onSuccess(new LinkedList<>());
                return;
            }

            List<Game> games = database.getGamesForPlatform(platform);

            subscriber.onSuccess(games);
        });
    }

    @Override
    public Single<List<Game>> query(String query) {
        return Single.create(subscriber -> {

            if(query == null || query.length() == 0){
                subscriber.onSuccess(new LinkedList<>());
                return;
            }

            List<Game> games = database.queryForGames(query);

            subscriber.onSuccess(games);
        });
    }

    @Override
    public Single<List<Game>> reloadGames(Platform... platform) {
        return Single.create(subscriber -> {
            //clean up possible removed files from content provider
            List<Game> games = database.getGames();

            removeGamesFromDatabase(games);

            final List<Game> updatedList = processNewGames(games);

            games.addAll(updatedList);

            Collections.sort(games, (o1, o2) -> o1.getName().compareTo(o2.getName()));

            subscriber.onSuccess(filter(Arrays.asList(platform), games));
        });
    }

    @Override
    public Single<List<Game>> reloadGames(String path, Platform... platform) {
        filesService.setCurrentDirectory(path);
        return reloadGames(platform);
    }

    private void removeGamesFromDatabase(List<Game> games){
        Stream.of(games)
                .filter(g -> !g.getFile().exists())
                .forEach(g -> {
                    games.remove(g);
                    database.delete(g);}
                );
    }

    private List<Game> processNewGames(List<Game> games){
        return Stream.of(filesService.getGameList())
                .map(f -> new Game(f.getAbsolutePath(), getPlatform(f)))
                .filter(f -> games.size() == 0 ||
                        Stream.of(games).anyMatch(g -> g.getFile().equals(f.getFile()) && g.needsUpdate()))
                .map(g -> {
                    Stream.of(games).filter(g1 -> g1.equals(g)).forEach(games::remove);

                    if (calculateMD5(g)) {
                        if(application.isConnectedToWeb())
                            searchWeb(g);
                        storeInDatabase(g);
                    }

                    return g;
                }).toList();

    }


    private List<Game> filter(List<Platform> platform, List<Game> games){
        return Stream.of(games)
                     .filter(g -> platform.contains(g.getPlatform()))
                     .toList();
    }

    private Platform getPlatform(File file) {
        final String fileExtension = FilesManager.getFileExtension(file);
        if(Platform.GBA.getExtensions().contains(fileExtension))
            return Platform.GBA;

        return Platform.GBC;
    }

    private void storeInDatabase(Game game){
        Log.v(TAG, "Storing recent acquired info on db!");
        database.insert(game);
    }

    private boolean searchWeb(Game game){
        try {
            final GameJSON json = application.getWebService()
                                      .getGameInformation(game.getMD5(), application.getDeviceLanguage())
                                      .execute()
                                      .body();

            if(json == null)
                return false;

            copyInformation(game, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean calculateMD5(Game game) {
        String md5 = Decoder.getFileMD5ToString(game.getFile());
        game.setMD5(md5);

        return md5 != null;
    }

    private void copyInformation(Game game, GameJSON json){
        game.setName(json.getName());
        game.setDescription(json.getDescription());
        game.setDeveloper(json.getDeveloper());
        game.setGenre(json.getGenre());
        game.setReleased(json.getReleased());
        game.setCoverURL(json.getCover());
    }
}
