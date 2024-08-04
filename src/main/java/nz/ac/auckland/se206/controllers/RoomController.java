package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.prompts.PromptEngineering;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.states.GameOver;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  @FXML private Rectangle rectOfficer;
  @FXML private Rectangle rectSuspect1;
  @FXML private Rectangle rectSuspect2;
  @FXML private Rectangle rectSuspect3;
  @FXML private Rectangle rectWaitress;
  @FXML private Button btnGuess;

  @FXML private TextArea textArea;
  @FXML private TextField inputField;
  @FXML private Button sendButton;
  @FXML private Pane chatContainer;
  @FXML private Label titleLabel;
  @FXML private Label chatTitle;

  @FXML private Label minutesLabel;
  @FXML private Label secondsLabel;
  private int remainingTime = 120;
  private Timeline timeline;

  private ChatCompletionRequest chatCompletionRequest;
  private String suspectResult;

  private GameStateContext context = new GameStateContext(this);

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    startCountdownTimer();
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    if(context.getCluesFound() >= 3) {
      titleLabel.setText("Click on any of the three suspects to make a guess!");
      chatContainer.setVisible(false);
      context.handleGuessClick();
    } else {
      titleLabel.setText("You need to find more clues! You have found " + context.getCluesFound() + "/3 clues!");
    }
  }

  private String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("suspectResult", suspectResult == "innocent" ? "not" : "");
    return PromptEngineering.getPrompt("chat.txt", map);
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setSuspectResult(String suspectResult) {
    this.suspectResult = suspectResult;
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      runGpt(new ChatMessage("system", getSystemPrompt()));
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    textArea.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private void runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);

    Task<ChatMessage> task = new Task<ChatMessage>() {
        @Override
        protected ChatMessage call() throws Exception {
            try {
                ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
                Choice result = chatCompletionResult.getChoices().iterator().next();
                chatCompletionRequest.addMessage(result.getChatMessage());
                return result.getChatMessage();
            } catch (ApiProxyException e) {
                e.printStackTrace();
                throw e;
            }
        }
    };

    task.setOnSucceeded(event -> {
        ChatMessage result = task.getValue();
        appendChatMessage(result);
        TextToSpeech.speak(result.getContent());
    });

    task.setOnFailed(event -> {
        Throwable exception = task.getException();
        exception.printStackTrace();
    });

    new Thread(task).start();
}

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = inputField.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    inputField.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onExit(ActionEvent event) throws ApiProxyException, IOException {
    chatContainer.setVisible(false);
    textArea.clear();
  }

  public void setChatContainerVisible(boolean visible) {
    chatContainer.setVisible(visible);
  }  

  public void setTitleLabelText(String text) {
    titleLabel.setText(text);
  }

  public void setChatTitleText(String text) {
    chatTitle.setText(text);
  }

  private void startCountdownTimer() {
   timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      remainingTime--;
      int minutes = (int) TimeUnit.SECONDS.toMinutes(remainingTime);
      int seconds = remainingTime - (minutes * 60);

      minutesLabel.setText(minutes + " minute" + (minutes == 1 ? "" : "s"));
      secondsLabel.setText(seconds + " second" + (seconds == 1 ? "" : "s"));

      if (remainingTime <= 0) {
        // Stop the timer when it reaches 0
        timeline.stop();
        context.setState(context.getGameOverState());
        minutesLabel.setText("Game Over,");
        secondsLabel.setText("Time's Up.");
        TextToSpeech.speak("Game Over, Time's Up, You lost.");
      }
    }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }
  
  public void setRemainingTime(Integer remInteger) {
    remainingTime = remInteger;
  }

  public void stopTimeline() {
    timeline.stop();
  }
}
