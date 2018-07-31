package com.codingame.game;
import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;
import vindinium.Board;
import vindinium.Config;
import vindinium.Hero;
import vindinium.Tile;
import vindinium.view.HeroHud;
import vindinium.view.ViewController;

import java.util.Properties;
import java.util.Random;

public class Referee extends AbstractReferee {
    // Uncomment the line below and comment the line under it to create a Solo Game
    // @Inject private SoloGameManager<Player> gameManager;
    @Inject
    private MultiplayerGameManager<Player> gameManager;
    @Inject
    private GraphicEntityModule graphicEntityModule;
    //@Inject
    //private TooltipModule tooltipModule;
    private HeroHud[] HeroHuds = new HeroHud[4];
    private Board board;
    private ViewController view;

    @Override
    public void init() {
        Properties params = gameManager.getGameParameters();
        System.err.println("seed: " + getSeed(params));
        Config.random = new Random(getSeed(params));
        board = Config.generateMap(gameManager.getPlayers());
        System.err.print(board.print());

        initGridView();
        board.initMines(graphicEntityModule, view.boardGroup);
        board.initHeroes(graphicEntityModule, view.boardGroup);
    }

    private void initGridView() {
        view = new ViewController(graphicEntityModule, gameManager);//, tooltipModule);
        view.createGrid(board);
        int c = 0;
        int remainingSpace =(graphicEntityModule.getWorld().getWidth()-graphicEntityModule.getWorld().getHeight());
        int rightXPos = remainingSpace/2+graphicEntityModule.getWorld().getHeight();
        for (Player p : gameManager.getPlayers()) {
            view.setSpawn(p.hero.spawnPos, p.getIndex());
            int w = graphicEntityModule.getWorld().getWidth();
            int h = graphicEntityModule.getWorld().getHeight();

            HeroHuds[c] = new HeroHud(p.hero, graphicEntityModule, p, c < 2 ? 0: (rightXPos), c%2==0?0:(h/2),(remainingSpace/2));
            c++;
        }
    }

    private void sendInputs(Player player, boolean initial) {
        System.err.println("input to player  " + player.getIndex());
        if (initial) {
            System.err.println(board.print().trim());
            System.err.println(player.getIndex());
        }
        System.err.println(board.boardState().trim());
        System.err.println("------------");

        if (initial) {
            player.sendInputLine(board.print().trim());
            player.sendInputLine(String.valueOf(player.getIndex()));
        }
        player.sendInputLine(board.boardState().trim());
    }

    @Override
    public void gameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        String action = "WAIT";
        if (player.isActive()) {
            try {
                sendInputs(player, turn < gameManager.getPlayerCount());
                player.execute();
                action = player.getOutputs().get(0).trim().toUpperCase();
            } catch (AbstractPlayer.TimeoutException timeout) {
                player.deactivate("timeout");
            }
        }
        Hero hero = player.hero;
        Tile target = hero.tile;
        String message = "";
        if (action.contains(" ")) {
            message = action.substring(action.indexOf(' ') + 1);
            action = action.substring(0, action.indexOf(' '));
        }
        if (action.equals("WAIT")) ;
        else if (action.equals("NORTH")) {
            if (target.y > 0) target = board.tiles[target.x][target.y - 1];
        } else if (action.equals("SOUTH")) {
            if (target.y + 1 < board.size) target = board.tiles[target.x][target.y + 1];
        } else if (action.equals("EAST")) {
            if (target.x + 1 < board.size) target = board.tiles[target.x + 1][target.y];
        } else if (action.equals("WEST")) {
            if (target.x > 0) target = board.tiles[target.x - 1][target.y];
        } else {
            player.deactivate("invalid action: \"" + action + "\"");
        }

        hero.move(board, target);
        hero.fight(board);
        hero.finalize(board);
        player.setScore(hero.gold);
        HeroHuds[player.getIndex()].OnRound(message);
    }

    private Long getSeed(Properties params) {
        return -4366460241541570794L;
        //try {
        //    return Long.parseLong(params.getProperty("seed", "0"));
        //} catch (NumberFormatException nfe) {
        //    return 0L;
        //}
    }
}
