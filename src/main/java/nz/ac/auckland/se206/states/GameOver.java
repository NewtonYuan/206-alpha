package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * The GameOver state of the game. Handles interactions after the game has ended, informing the
 * player that the game is over and no further actions can be taken.
 */
public class GameOver implements GameState {

  private final GameStateContext context;
  private final RoomController roomController;

  /**
   * Constructs a new GameOver state with the given game state context.
   *
   * @param context the context of the game state
   */
  public GameOver(GameStateContext context, RoomController roomController) {
    this.context = context;
    this.roomController = roomController;
  }

  /**
   * Handles the event when a rectangle is clicked. Informs the player that the game is over and
   * provides the profession of the clicked character if applicable.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    switch (rectangleId) {
      case "rectOfficer":
        return;
      case "rectClueCar":
        return;
      case "rectClueMoney":
        return;
      case "rectClueGate":
        return;
    }
    String suspectResult = context.getSuspectResult(rectangleId);
    roomController.setTitleLabelText("The investigator is thinking...");
    TextToSpeech.speak(
        "Game Over, you have already guessed! This is"
            + (suspectResult == "innocent" ? " not" : "")
            + " the thief",
        () ->
            roomController.setTitleLabelText(
                "Game Over, you have already guessed! This is"
                    + (suspectResult == "innocent" ? " not" : "")
                    + " the thief"));
  }

  /**
   * Handles the event when the guess button is clicked. Informs the player that the game is over
   * and no further guesses can be made.
   *
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleGuessClick() throws IOException {
    TextToSpeech.speak("You have already guessed!", () -> {});
  }
}
