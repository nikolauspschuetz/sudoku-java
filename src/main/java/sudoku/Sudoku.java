package sudoku;


import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

import java.io.IOException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Sudoku {

  // private static String DEFAULT_PUZZLE = "puzzles/sudoku-com-easy-20230126T132000.txt";
  private static String DEFAULT_PUZZLE = "puzzles/sudoku-com-evil-20221216T173500.txt";

  private static ArgumentParser parser = ArgumentParsers
  .newFor("sudoku")
  .build()
  .defaultHelp(true);
  
  
  private static Namespace parseArgs(String[] args) throws ArgumentParserException {
    parser.addArgument("-F", "--file")
    .help("Sudoku puzzle file")
    // .action(storeTrue());
    .setDefault(DEFAULT_PUZZLE);
    
    return parser.parseArgs(args);
  }

  public static void main(String[] args) {
    try {
      Namespace res = parseArgs(args);
      String filename = res.get("file");
      Board board = Board.fromFile(filename);
      board.printBoard();
      if (board.solve()) System.out.println("solved board");
      else System.out.println("failed to solve board");
      board.printBoard();
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    } catch (IOException e) {
      System.exit(1);
    } catch (BoardException be) {
      System.exit(1);
    }
  }
}

