package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * The Guessing state of the game. Handles the logic for when the player is
 * making a guess about the
 * profession of the characters in the game.
 */
public class Guessing implements GameState {

    private final GameStateContext context;
    private final RoomController roomController;

    /**
     * Constructs a new Guessing state with the given game state context.
     *
     * @param context the context of the game state
     */
    public Guessing(GameStateContext context, RoomController roomController) {
        this.context = context;
        this.roomController = roomController;
    }

    /**
     * Handles the event when a rectangle is clicked. Checks if the clicked
     * rectangle is a customer
     * and updates the game state accordingly.
     *
     * @param event       the mouse event triggered by clicking a rectangle
     * @param rectangleId the ID of the clicked rectangle
     * @throws IOException if there is an I/O error
     */
    @Override
    public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
        if (rectangleId.equals("rectOfficer")) {
            TextToSpeech.speak("You should click on the suspects");
            return;
        }

        if (rectangleId.equals(context.getRectIdToGuess())) {
            roomController.setTitleLabelText("Correct! You won! This is the thief!");
            TextToSpeech.speak("Correct! You won! This is the thief!");
            roomController.setGameOverText("You guessed the right person.");
        } else {
            roomController.setTitleLabelText("You lost! This is not the thief!");
            TextToSpeech.speak("You lost! This is not the thief!");
            roomController.setGameOverText("You guessed the wrong person.");
        }
        context.setState(context.getGameOverState());
        roomController.stopTimeline();
        roomController.setGameOverVisible(true);
    }

    /**
     * Handles the event when the guess button is clicked. Since the player has
     * already guessed, it
     * notifies the player.
     *
     * @throws IOException if there is an I/O error
     */
    @Override
    public void handleGuessClick() throws IOException {
        TextToSpeech.speak("You have already guessed!");
    }
}
