package com.codingame.game;


import com.codingame.gameengine.module.entities.Group;

public interface IView {
    void onRound();
    Group getView();
}