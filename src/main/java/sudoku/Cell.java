package sudoku;

import java.util.Set;


public class Cell {
    
    int row;
    int col;
    Set<Integer> solutions;

    public Cell(int row, int col, Set<Integer> solutions) {
        this.row = row;
        this.col = col;
        this.solutions = solutions;
    }
}
