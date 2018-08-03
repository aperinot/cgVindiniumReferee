package com.codingame.game;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Sprite;
import vindinium.Hero;
import com.codingame.game.view.TileFactory;
import com.codingame.game.view.ViewController;
import vindinium.Tile;

public class HeroView implements IView{
    private int _healthBarHeight = 32;
    public Hero _model;
    private Sprite _sprite;
    private Group _group;
    private GraphicEntityModule _entityManager;
    private Tile _lastTile;
    private int _lastDir = -1;
    private Rectangle _healthBar;

    public HeroView(Hero model, GraphicEntityModule entityManager){
        _model = model;
        _entityManager = entityManager;
        _group = entityManager.createGroup()
                .setX(ViewController.CELL_SIZE * (model.tile.x + 1) - 4)
                .setY(ViewController.CELL_SIZE * (model.tile.y + 1) - 4).setZIndex(10);

        _sprite = entityManager.createSprite()
                .setImage(TileFactory.getInstance().heroes[model.player.getIndex() * 9])
                .setBaseHeight(ViewConstants.SPRITE_SIZE)
                .setBaseWidth(ViewConstants.SPRITE_SIZE)
                .setAlpha(1.0)
                .setZIndex(-1);
        _group.add(_sprite);

        _group.add(entityManager.createRectangle().setWidth(3).setHeight(_healthBarHeight).setX(-5).setFillColor(0xff0000));
        _group.add(_healthBar = entityManager.createRectangle().setWidth(3).setHeight(_healthBarHeight).setX(-5).setFillColor(0x00ff00));
    }

    @Override
    public void onRound() {
        if(_model.wasDead){
            _entityManager.commitEntityState(0.8, _sprite, _group);
            _sprite.setImage(TileFactory.getInstance().heroes[4 * 9 + _model.lastDir]);
            _group.setX(ViewController.CELL_SIZE * (_model.tile.x + 1) - 4)
                    .setY(ViewController.CELL_SIZE * (_model.tile.y + 1) - 4);

            _entityManager.commitEntityState(1.0, _sprite, _group);
        }
        else
        {
            if(_lastDir != _model.lastDir) {
                _lastDir = _model.lastDir;
                _sprite.setImage(TileFactory.getInstance().heroes[_model.player.getIndex() * 9 + _model.lastDir]);
                _entityManager.commitEntityState(0, _sprite);
            }

            if(_model.tile != _lastTile) {
                _lastTile = _model.tile;
                _group.setX(ViewController.CELL_SIZE * (_model.tile.x + 1) - 4)
                        .setY(ViewController.CELL_SIZE * (_model.tile.y + 1) - 4);
            }
        }

        _healthBar.setHeight((int)(_healthBarHeight*_model.life/100.0));
    }

    @Override
    public Group getView() {
        return _group;
    }
}
