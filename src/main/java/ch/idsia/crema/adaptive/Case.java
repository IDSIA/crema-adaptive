package ch.idsia.crema.adaptive;

/*
Source: https://codereview.stackexchange.com/a/195433
*/
public class Case {

    private int row;
    private int col;
    private int value;

    public Case(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    static Case findHighestValue2D(int[][] values) {
        Case highestCase = new Case(-1, -1, Integer.MIN_VALUE);
        Case initialHighestCase = highestCase;

        for (int row = 0; row < values.length; row++) {
            for (int col = 0; col < values[row].length; col++) {
                int value = values[row][col];
                if (value > highestCase.getValue()) {
                    highestCase = new Case(row, col, value);
                }
            }
        }

        if (highestCase == initialHighestCase) {
            return null;
        } else {
            return highestCase;
        }
    }

    static Case findHighestValue1D(int[] values) {
        Case highestCase = new Case(-1, -1, Integer.MIN_VALUE);
        Case initialHighestCase = highestCase;

        for (int row = 0; row < values.length; row++) {
            int value = values[row];
            if (value > highestCase.getValue()) {
                highestCase = new Case(row, 0, value);
            }
        }

        if (highestCase == initialHighestCase) {
            return null;
        } else {
            return highestCase;
        }

    }
}
