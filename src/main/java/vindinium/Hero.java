package vindinium;

import com.codingame.game.Player;

public class Hero {
    public Player player;
    public Tile tile;
    public Tile spawnPos;
    public int lastDir;
    public int life;
    public int gold;
    public boolean wasDead = false;
    private boolean justRespawned = false;

    public static final int maxLife = 100;
    static final int beerLife = 50;
    static final int beerGold = 2;
    static final int dayLife = 1;
    static final int mineLife = 20;
    static final int defendLife = 20;

    public Hero(Player player, Tile spawnPos) {
        this.player = player;
        this.spawnPos = spawnPos;
        this.tile = spawnPos;
        this.life = maxLife;
    }

    public void drinkBeer() {
        if (gold >= beerGold) {
            gold -= beerGold;
            life += beerLife;
            if (life > maxLife) life = maxLife;
        }
    }

    public void day() {
        if (life > dayLife) life -= dayLife;
    }

    public void respawn(Board board) {
        justRespawned = true;
        wasDead = true;
        life = maxLife;
        tile = spawnPos;


        for (Hero h : board.heroes) {
            if (h == this || tile != h.tile) continue;
            board.transferMines(h, this);
            h.respawn(board);
        }
    }

    public void fightMine(Board board, Tile target) {
        if (target.mine.owner == this) return;
        life -= mineLife;
        if (life > 0) {
            target.mine.conquer(this);
        } else {
            board.transferMines(this, null);
            respawn(board);
        }
    }

    public void fight(Board board) {
        wasDead = false;
        for (Hero h : board.heroes) {
            if (tile.distance(h.tile) != 1 || h.justRespawned) continue;
            h.defend();
            if (h.life <= 0) {
                board.transferMines(h, this);
                h.respawn(board);
            }
        }
    }

    public void defend() {
        life -= defendLife;
    }

    public void move(Board board, Tile target) {
        if (target.type == Tile.Type.Wall) {
            return;
        }

        if (target.type == Tile.Type.Tavern) {
            drinkBeer();
        }
        else if (target.type == Tile.Type.Mine) {
            fightMine(board, target);
        }
        else {
            if (tile.y < target.y) lastDir = 0;
            else if (tile.x > target.x) lastDir = 1;
            else if (tile.x < target.x) lastDir = 2;
            else if (tile.y > target.y) lastDir = 3;
            final Tile t = target;
            if (board.heroes.stream().anyMatch(h -> h.tile == t)) {
                target = tile; // can't share position with other hero
            }
            tile = target;
        }
    }

    public void finalize(Board board) {
        justRespawned = false;
        day();
        gold += board.countMines(this);
    }

    public String print() {
        return "HERO " + player.getIndex() + " " + tile.x + " " + tile.y + " " + life + " " + gold;
    }
}