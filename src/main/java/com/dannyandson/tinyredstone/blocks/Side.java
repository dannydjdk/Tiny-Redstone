package com.dannyandson.tinyredstone.blocks;

public enum Side {
    FRONT, RIGHT, BACK, LEFT, TOP, BOTTOM;

    public Side getOpposite() {
        switch (this){
            case FRONT:return BACK;
            case RIGHT:return LEFT;
            case BACK:return FRONT;
            case LEFT:return RIGHT;
            case TOP:return BOTTOM;
            case BOTTOM:return TOP;
        }
        return null;
    }
    public Side rotateYCW() {
        switch (this){
            case FRONT:return RIGHT;
            case RIGHT:return BACK;
            case BACK:return LEFT;
            case LEFT:return FRONT;
        }
        return null;
    }
    public Side rotateYCCW() {
        switch (this){
            case FRONT:return LEFT;
            case RIGHT:return FRONT;
            case BACK:return RIGHT;
            case LEFT:return BACK;
        }
        return null;
    }


}
