package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * The GameStarted state of the game. Handles the initial interactions when the game starts,
 * allowing the player to chat with characters and prepare to make a guess.
 */
public class GameStarted implements GameState {

  private final GameStateContext context;
  private final RoomController roomController;

  /**
   * Constructs a new GameStarted state with the given game state context.
   *
   * @param context the context of the game state
   */
  public GameStarted(GameStateContext context, RoomController roomController) {
    this.context = context;
    this.roomController = roomController;
  }

  /**
   * Handles the event when a rectangle is clicked. Depending on the clicked rectangle, it either
   * provides an introduction or transitions to the chat view.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    // Transition to chat view or provide an introduction based on the clicked
    // rectangle
    roomController.setTitleLabelText("The investigator is thinking...");
    switch (rectangleId) {
      case "rectOfficer":
        TextToSpeech.speak(
            "This is you, collect clues to find the thief",
            () -> roomController.setTitleLabelText("This is you."));
        return;
      case "rectClueCar":
        TextToSpeech.speak(
            "The thief seems to have used a car as transport.",
            () ->
                roomController.setTitleLabelText(
                    "Clue found: The thief seems to have used a car as transport."));
        context.updateCluesFound(1);
        return;
      case "rectClueMoney":
        TextToSpeech.speak(
            "The thief seems to have tried to steal money.",
            () ->
                roomController.setTitleLabelText(
                    "Clue found: The thief seems to have tried to steal money."));
        context.updateCluesFound(1);
        return;
      case "rectClueGate":
        TextToSpeech.speak(
            "The thief seems to have tried to open this gate.",
            () ->
                roomController.setTitleLabelText(
                    "Clue found: The thief seems to have tried to open this gate."));
        context.updateCluesFound(1);
        return;
      case "rectSuspect1":
        roomController.setChatTitleText("Suspect ONE");
        context.updateSuspectsConversed(1);
      case "rectSuspect2":
        roomController.setChatTitleText("Suspect TWO");
        context.updateSuspectsConversed(1);
      case "rectSuspect3":
        roomController.setChatTitleText("Suspect THREE");
        context.updateSuspectsConversed(1);
    }
    roomController.setChatContainerVisible(true);
    roomController.setTitleLabelText("You are talking with a suspect...");
    roomController.setSuspectResult(context.getSuspectResult(rectangleId));
  }

  /**
   * Handles the event when the guess button is clicked. Prompts the player to make a guess and
   * transitions to the guessing state.
   *
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleGuessClick() throws IOException {
    roomController.setTitleLabelText("The investigator is thinking...");
    TextToSpeech.speak(
        "Make a guess, click on the correct suspect",
        () -> {
          roomController.setTitleLabelText("Click on any of the three suspects to make a guess!");
          context.setState(context.getGuessingState());
          roomController.setRemainingTime(10);
        });
  }
}
