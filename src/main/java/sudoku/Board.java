package sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Board {

  int[][] board = new int[9][9];
  int depth = 0;

  public static int[][] readBoard(String filename) throws IOException, BoardException {
    int[][] b = new int[9][9];
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(filename));
      String line = reader.readLine();
      int r = 0;
      while (line != null) {
        // System.out.println(line);
        if (line.length() != 0 && line.charAt(0) != '#') {
          if (r > 8)
            throw new BoardException("Too many rows in board.");
          int c = 0;
          String[] split = line.replaceAll("\\s", "").split(",");
          if (split.length != 9)
            throw new BoardException(
                String.format("Wrong number (%d) of characters character in row %d", split.length, r));
          for (String s : split) {
            if (!Utils.isInteger(s))
              throw new BoardException(String.format("Invalid character %s in row %d", s, r));
            int v = Integer.parseInt(s);
            if (v < 0)
              v = 0;
            b[r][c] = v;
            c++;
          }
          r++;
        }
        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
    return b;
  }

  public void printBoard() {
    for (int[] row : board) {
      String[] sa = new String[9];
      for (int i = 0; i < 9; i++)
        sa[i] = String.format("%d", row[i]);
      System.out.println(String.join(" ", sa));
    }
  }

  private static int[][] indices() {
    int[][] rc = new int[81][2];
    for (int r = 0, n = 0; r < 9; r++)
      for (int c = 0; c < 9; c++) {
        rc[n][0] = r;
        rc[n][1] = c;
        n++;
      }
    return rc;
  }

  public int[] getRow(int r) {
    int[] row = new int[9];
    for (int c = 0; c < 9; c++)
      row[c] = board[r][c];
    return row;
  }

  public int[] getCol(int c) {
    int[] col = new int[9];
    for (int r = 0; r < 9; r++)
      col[r] = board[r][c];
    return col;
  }

  public int[] getBox(int rb, int cb) {
    int[] box = new int[9];
    int i = 0;
    for (int r = rb * 3; r < rb * 3 + 3; r++)
      for (int c = cb * 3; c < cb * 3 + 3; c++) {
        box[i] = board[r][c];
        i++;
      }
    ;
    return box;
  }

  public boolean done() {
    for (int[] rc : indices()) {
      int r = rc[0], c = rc[1];
      if (board[r][c] == 0)
        return false;
    }
    return true;
  }

  private static boolean valid(int[] a) {
    int[] counts = new int[9];
    for (int n : a) {
      if (n > 0) {
        int i = n - 1;
        counts[i]++;
        if (counts[i] > 1)
          return false;
      }
    }
    return true;
  }

  public boolean valid() {
    for (int i = 0; i < 9; i++) {
      if (!valid(getRow(i)) || !valid(getCol(i)))
        return false;
    }
    for (int rb = 0; rb < 3; rb++)
      for (int cb = 0; cb < 3; cb++) {
        if (!valid(getBox(rb, cb)))
          return false;
      }
    return true;
  }

  private boolean doneAndValid() {
    return done() && valid();
  }

  private static List<Integer> asList(int[] a) {
    return Arrays.asList(Arrays.stream(a).boxed().toArray(Integer[]::new));
  }

  private Set<Integer> solutions(int r, int c) {
    Set<Integer> options = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    Set<Integer> taken = new HashSet<>(asList(getRow(r)));
    taken.addAll(asList(getCol(c)));
    taken.addAll(asList(getBox(r / 3, c / 3)));
    options.removeAll(taken);
    return options;
  }

  private boolean solve1() {
    for (int[] rc : indices()) {
      int r = rc[0], c = rc[1];
      if (board[r][c] == 0) {
        Set<Integer> sol = solutions(r, c);
        int n = sol.size();
        if (n == 0)
          return false;
        if (n == 1) {
          int v = sol.iterator().next();
          board[r][c] = v;
          return true;
        }
      }
    }
    return false;
  }

  private ArrayList<Cell> getUnsolved() throws BoardException {
    ArrayList<Cell> unsolved = new ArrayList<Cell>();
    for (int[] rc : indices()) {
      int r = rc[0], c = rc[1];
      if (board[r][c] == 0) {
        Set<Integer> s = solutions(r, c);
        if (s.size() < 1) {
          String msg = String.format("Unable to find backtracking space for %d, %d: %s", r, c, s);
          throw new BoardException(msg);
        }
        unsolved.add(new Cell(r, c, s));
      }
    }
    // sort according to number of solutions
    Collections.sort(unsolved, new Comparator<Cell>() {
      @Override
      public int compare(Cell lhs, Cell rhs) {
        int l = lhs.solutions.size(), r = rhs.solutions.size();
        return l > r ? 1 : l < r ? -1 : 0;
      }
    });
    return unsolved;
  }

  private boolean backtrack() {
    Board backup = copy();
    ArrayList<Cell> unsolved;
    try {
      unsolved = getUnsolved();
    } catch (BoardException be) {
      return false;
    }
    Cell cell = unsolved.iterator().next();
    depth++;
    for (int v : cell.solutions) {
      int r = cell.row, c = cell.col;
      board[r][c] = v;
      if (solve()) {
        // System.out.println(String.format("backtracking on %d, %d option %d succeeded
        // at depth %d", r, c, v, depth));
        depth--;
        return true;
      } else {
        reset(backup);
      }
    }
    return false;
  }

  public boolean solve() {
    while (!doneAndValid()) {
      if (!solve1()) {
        if (!backtrack())
          break;
      }
    }
    return doneAndValid();
  }

  private Board(int[][] board) {
    this.board = board;
  }

  private Board copy() {
    int[][] b = new int[9][9];
    for (int i = 0; i < 9; i++)
      b[i] = Arrays.copyOf(board[i], 9);
    return new Board(b);
  }

  private void reset(Board other) {
    for (int i = 0; i < 9; i++)
      board[i] = Arrays.copyOf(other.board[i], 9);
  }

  public static Board fromFile(String filename) throws IOException, BoardException {
    return new Board(readBoard(filename));
  }

}
